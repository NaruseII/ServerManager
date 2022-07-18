package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.api.config.Configuration;
import fr.naruse.servermanager.core.ServerManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketCreateTemplate implements IPacket {

    public PacketCreateTemplate() {
    }

    private String fileName;
    private Configuration templateConfiguration;

    public PacketCreateTemplate(String fileName, Configuration templateConfiguration) {
        this.fileName = fileName;
        this.templateConfiguration = templateConfiguration;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeUTF(this.fileName);

        PacketUtils.writeByteArray(stream, this.templateConfiguration.toJson().getBytes());
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.fileName = stream.readUTF();

        this.templateConfiguration = new Configuration(new String(PacketUtils.readByteArray(stream)));
    }

    @Override
    public void process(ServerManager serverManager) {

    }

    public Configuration getTemplateConfiguration() {
        return templateConfiguration;
    }

    public String getFileName() {
        return fileName;
    }
}
