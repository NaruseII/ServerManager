package fr.naruse.servermanager.core.connection;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.api.events.packet.AsyncPacketReceiveEvent;
import fr.naruse.servermanager.core.api.events.packet.AsyncPacketSendEvent;
import fr.naruse.servermanager.core.config.Configuration;
import fr.naruse.servermanager.core.connection.packet.*;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.utils.CustomRunnable;
import fr.naruse.servermanager.core.utils.ThreadLock;
import fr.naruse.servermanager.core.utils.Utils;
import fr.naruse.servermanager.filemanager.ServerProcess;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionManager {

    private static final ServerManagerLogger.Logger LOGGER = new ServerManagerLogger.Logger("ConnectionManager");
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private final ServerManager serverManager;

    private int localPort;

    public ConnectionManager(ServerManager serverManager) {
        LOGGER.info("Starting ConnectionManager...");
        this.serverManager = serverManager;

        if(!this.serverManager.getCoreData().getCoreServerType().is(CoreServerType.PACKET_MANAGER)){
            LOGGER.info("Packet-Manager is '"+this.serverManager.getCoreData().getPacketManagerHost()+":"+serverManager.getCoreData().getPacketManagerPort()+"'");
        }else{
            Configuration configuration = this.serverManager.getConfigurationManager().getConfig();
            configuration.getSection("packet-manager").set("serverAddress", this.serverManager.getCoreData().getCurrentAddress());
            configuration.save();
        }

        LOGGER.info("Starting server thread...");
        this.startServerThread();

        LOGGER.info("ConnectionManager started");
    }

    private void startServerThread() {
        EXECUTOR_SERVICE.submit(() -> {
            try {
                boolean isPacketManager = this.serverManager.getCoreData().getCoreServerType().is(CoreServerType.PACKET_MANAGER);

                ServerSocket serverSocket;
                if(isPacketManager){
                    serverSocket = new ServerSocket(isPacketManager ? this.serverManager.getCoreData().getPacketManagerPort() : this.serverManager.getCoreData().getServerManagerPort(), 50, Utils.findHost("0.0.0.0", false));
                }else{
                    serverSocket = new ServerSocket(isPacketManager ? this.serverManager.getCoreData().getPacketManagerPort() : this.serverManager.getCoreData().getServerManagerPort());
                }

                LOGGER.info((isPacketManager ? "Server":"Client")+" thread started");
                LOGGER.info("Listening on '"+serverManager.getCoreData().getCurrentAddress()+":"+(this.localPort = serverSocket.getLocalPort())+"'");
                if(this.serverManager.getCoreData().getPacketManagerPort() == 0){
                    this.serverManager.getCoreData().setPacketManagerPort(this.localPort);
                }

                this.serverManager.getCurrentServer().setServerManagerPort(this.localPort);
                if(!isPacketManager){
                    this.sendPacket(new PacketConnection(this.serverManager.getCurrentServer()));
                }

                while (true){
                    Socket socket = serverSocket.accept();

                    EXECUTOR_SERVICE.submit(() -> {
                        try {
                            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                            String packetName = dataInputStream.readUTF();
                            String sentKey = dataInputStream.readUTF();
                            if(!this.serverManager.getConfigurationManager().getConfig().get("key").equals(sentKey)){
                                LOGGER.error("A packet was received with a wrong secret! "+socket.getInetAddress().getHostAddress()+":"+socket.getPort()+" -> SecretKey: "+sentKey+" -> Packet '"+packetName+"' blocked!");
                                return;
                            }

                            IPacket packet = Packets.buildPacket(packetName);
                            packet.read(dataInputStream);

                            AsyncPacketReceiveEvent event = new AsyncPacketReceiveEvent(packet, packetName);
                            this.serverManager.getPlugin().callEvent(event);
                            if(event.isCancelled()){
                                return;
                            }

                            packet.process(this.serverManager);

                            if(packet instanceof AbstractPacketResponsive){
                                ThreadLock.unlock((AbstractPacketResponsive) packet);
                            }
                            this.serverManager.processPacket(packet);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendPacketToAll(IPacket packet){
        this.sendPacket(ServerList.getAll(), packet);
    }

    public void sendPacket(Server server, IPacket packet){
        this.sendPacket(packet, server.getAddress(), server.getServerManagerPort());
    }

    public void sendPacket(Set<Server> servers, IPacket packet){
        servers.forEach(server -> this.sendPacket(packet, server.getAddress(), server.getServerManagerPort()));
    }

    // to Packet-Manager
    public void sendPacket(IPacket packet){
        this.sendPacket(packet, Utils.getPacketManagerHost(), this.serverManager.getCoreData().getPacketManagerPort());
    }

    public void sendResponsivePacket(Thread thread, AbstractPacketResponsive packet, CustomRunnable<AbstractPacketResponsive> onResponse){
        this.sendPacket(packet);
        ThreadLock.lock(thread, onResponse);
    }

    private int retryCount = 0;
    private void sendPacket(IPacket packet, InetAddress inetAddress, int port){
        EXECUTOR_SERVICE.submit(() -> {
            try {
                String packetName = Packets.getNameByPacket(packet.getClass());

                AsyncPacketSendEvent event = new AsyncPacketSendEvent(packet, packetName, inetAddress, port);
                this.serverManager.getPlugin().callEvent(event);
                if(event.isCancelled()){
                    return;
                }
                InetAddress finalInetAddress = event.getDestinationAddress();

                int finalPort = event.getDestinationPort();

                Socket socket = new Socket(finalInetAddress, finalPort);

                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF(packetName);

                String secretKet = this.serverManager.getConfigurationManager().getConfig().get("key");
                if(secretKet == null){
                    LOGGER.error("Secret key is null! Stopping...");
                    ServerProcess.BE_PATIENT = true;
                    System.exit(1);
                    return;
                }

                dataOutputStream.writeUTF(secretKet);
                packet.write(dataOutputStream);

                socket.close();
            } catch (Exception e) {
                if(e.getClass().isAssignableFrom(ConnectException.class)){
                    if(this.serverManager.isShuttingDowned()){
                        return;
                    }
                    if(port == this.serverManager.getCoreData().getPacketManagerPort()){
                        this.retryCount++;

                        if(this.serverManager.isShuttingDowned()){
                            return;
                        }

                        int count = this.serverManager.getCoreData().getCoreServerType().is(CoreServerType.FILE_MANAGER) ? 20 : 3;
                        if(this.retryCount == count){
                            LOGGER.error("Can't connect to Packet-Manager!");
                            LOGGER.error("");
                            LOGGER.warn("Shutting down...");
                            ServerProcess.BE_PATIENT = true;
                            if(!this.serverManager.getCoreData().getCoreServerType().is(CoreServerType.PACKET_MANAGER, CoreServerType.FILE_MANAGER)){
                                this.serverManager.getPlugin().shutdown();
                            }else{
                                System.exit(1);
                            }
                        }else if(this.retryCount <= count){
                            LOGGER.error("Couldn't send packet to ["+inetAddress.getHostAddress()+":"+port+"] !");
                            LOGGER.warn("Retrying... ("+this.retryCount+"/"+count+")");
                        }
                    }else{
                        LOGGER.error("Couldn't send packet to ["+inetAddress.getHostAddress()+":"+port+"] !");
                    }
                }else{
                    e.printStackTrace();
                }
            }
        });
    }

    public void shutdown() {
        if(this.serverManager.getCoreData().getCoreServerType() != CoreServerType.PACKET_MANAGER){
            this.sendPacket(new PacketDisconnection(this.serverManager.getCurrentServer()));
            LOGGER.info("Sending disconnection packet...");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LOGGER.info("Stopping connection thread pool...");
        EXECUTOR_SERVICE.shutdown();
    }
}
