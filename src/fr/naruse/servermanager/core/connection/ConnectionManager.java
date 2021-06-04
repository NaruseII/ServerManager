package fr.naruse.servermanager.core.connection;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.api.events.packet.AsyncPacketReceiveEvent;
import fr.naruse.servermanager.core.api.events.packet.AsyncPacketSendEvent;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.connection.packet.IPacket;
import fr.naruse.servermanager.core.connection.packet.PacketConnection;
import fr.naruse.servermanager.core.connection.packet.PacketDisconnection;
import fr.naruse.servermanager.core.connection.packet.Packets;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.ServerList;

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

    private InetAddress inetAddress;
    private int localPort;

    public ConnectionManager(ServerManager serverManager) {
        LOGGER.info("Starting ConnectionManager...");
        this.serverManager = serverManager;

        try {
            this.inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        LOGGER.info("Local address is '"+this.inetAddress.getHostAddress()+"'");

        LOGGER.info("Starting server thread...");
        this.startServerThread();

        LOGGER.info("ConnectionManager started");
    }

    private void startServerThread() {
        EXECUTOR_SERVICE.submit(() -> {
            try {
                boolean flag = this.serverManager.getCoreData().getCoreServerType().is(CoreServerType.PACKET_MANAGER);

                ServerSocket serverSocket = new ServerSocket(flag ? this.serverManager.getCoreData().getServerPort() : this.serverManager.getCoreData().getServerManagerPort());
                LOGGER.info((flag ? "Server":"Client")+" thread started");
                LOGGER.info("Listening on port "+(this.localPort = serverSocket.getLocalPort()));
                if(this.serverManager.getCoreData().getServerPort() == 0){
                    this.serverManager.getCoreData().setServerPort(this.localPort);
                }

                this.serverManager.getCurrentServer().setServerManagerPort(this.localPort);
                if(!flag){
                    this.serverManager.getConnectionManager().sendPacket(new PacketConnection(this.serverManager.getCurrentServer()));
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
        this.sendPacket(packet, this.inetAddress, server.getServerManagerPort());
    }

    public void sendPacket(Set<Server> servers, IPacket packet){
        servers.forEach(server -> this.sendPacket(packet, this.inetAddress, server.getServerManagerPort()));
    }

    // to Packet-Manager
    public void sendPacket(IPacket packet){
        this.sendPacket(packet, this.inetAddress, this.serverManager.getCoreData().getServerPort());
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
                    System.exit(0);
                    return;
                }

                dataOutputStream.writeUTF(secretKet);
                packet.write(dataOutputStream);

                socket.close();
            } catch (Exception e) {
                if(e.getClass().isAssignableFrom(ConnectException.class)){
                    LOGGER.error("Couldn't send packet to ["+inetAddress.getHostAddress()+":"+port+"] !");
                    if(port == this.serverManager.getCoreData().getServerPort()){
                        LOGGER.warn("Retrying... ("+this.retryCount+"/5");
                        this.retryCount++;
                        if(this.retryCount >= 5){
                            LOGGER.error("Can't connect to Packet-Manager!");
                            LOGGER.error("");
                            LOGGER.warn("Shutting down...");
                            System.exit(0);
                        }
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
