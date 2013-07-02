package net.minecraft.server;

import java.io.DataInput;
import java.io.DataOutput;

import java.io.IOException; // CraftBukkit

public class Packet2Handshake extends Packet {

    private int a;
    private String b;
    public String c; // CraftBukkit private -> public
    public int d; // CraftBukkit private -> public

    public Packet2Handshake() {}

    public static final java.util.regex.Pattern validName = java.util.regex.Pattern.compile("^[a-zA-Z0-9_-]{2,16}$");
    public void a(DataInput datainput) throws IOException { // CraftBukkit - throws IOException
        this.a = datainput.readByte();
        this.b = a(datainput, 16);
        this.c = a(datainput, 255);
        this.d = datainput.readInt();
        // Spigot: TODO: md_5 hates this
        if(!validName.matcher(this.b).matches()) throw new IOException("Invalid name!"); // Spigot
    }

    public void a(DataOutput dataoutput) throws IOException { // CraftBukkit - throws IOException
        dataoutput.writeByte(this.a);
        a(this.b, dataoutput);
        a(this.c, dataoutput);
        dataoutput.writeInt(this.d);
    }

    public void handle(Connection connection) {
        connection.a(this);
    }

    public int a() {
        return 3 + 2 * this.b.length();
    }

    public int d() {
        return this.a;
    }

    public String f() {
        return this.b;
    }
}
