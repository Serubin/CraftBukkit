package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random; // Tyr
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Packet51MapChunk extends Packet {

    public int a;
    public int b;
    public int c;
    public int d;
    public byte[] buffer;
    public boolean f;
    public int size; // CraftBukkit - private -> public
    private int h;
    public byte[] rawData = new byte[0]; // CraftBukkit

    // begin Tyr
    public static int OreObfusicationEngine = 1;
    public static byte[] transparentIds = { 8, 9, 10, 11, 20, 27, 28, 37, 38, 39, 40, 44, 50, 53, 55, 63, 64, 65, 66, 67, 68, 69, 71, 75, 76, 77, 78, 79, 85, 96 };
    public static boolean runchestlogic = true;
    public static byte maingroundblock = 1;

    private static final byte[] fakeBlocks = { 56, 14, 15, 16, 73, 21, 48, 97, 98 };
    private static final int numFakeBlocks = fakeBlocks.length;
    private static final Random obRand = new Random();
    public World world;
    // end Tyr

    public Packet51MapChunk() {
        this.lowPriority = true;
    }

    // CraftBukkit start
    public Packet51MapChunk(Chunk chunk, boolean flag, int i) {
        this.world = chunk.world; // Tyr
        this.lowPriority = true;
        this.a = chunk.x;
        this.b = chunk.z;
        this.f = flag;
        if (flag) {
            i = '\uffff';
            chunk.seenByPlayer = true;
        }

        ChunkSection[] achunksection = chunk.h();
        int j = 0;
        int k = 0;

        int l;

        for (l = 0; l < achunksection.length; ++l) {
            if (achunksection[l] != null && (!flag || !achunksection[l].a()) && (i & 1 << l) != 0) {
                this.c |= 1 << l;
                ++j;
                if (achunksection[l].h() != null) {
                    this.d |= 1 << l;
                    ++k;
                }
            }
        }

        l = 2048 * (5 * j + k);
        if (flag) {
            l += 256;
        }

        if (rawData.length < l) {
            rawData = new byte[l];
        }

        byte[] abyte = rawData;
        int i1 = 0;

        int j1;

        for (j1 = 0; j1 < achunksection.length; ++j1) {
            if (achunksection[j1] != null && (!flag || !achunksection[j1].a()) && (i & 1 << j1) != 0) {
                byte[] abyte1 = achunksection[j1].g();

                System.arraycopy(abyte1, 0, abyte, i1, abyte1.length);
                i1 += abyte1.length;
            }
        }

        NibbleArray nibblearray;

        for (j1 = 0; j1 < achunksection.length; ++j1) {
            if (achunksection[j1] != null && (!flag || !achunksection[j1].a()) && (i & 1 << j1) != 0) {
                nibblearray = achunksection[j1].i();
                System.arraycopy(nibblearray.a, 0, abyte, i1, nibblearray.a.length);
                i1 += nibblearray.a.length;
            }
        }

        for (j1 = 0; j1 < achunksection.length; ++j1) {
            if (achunksection[j1] != null && (!flag || !achunksection[j1].a()) && (i & 1 << j1) != 0) {
                nibblearray = achunksection[j1].j();
                System.arraycopy(nibblearray.a, 0, abyte, i1, nibblearray.a.length);
                i1 += nibblearray.a.length;
            }
        }

        for (j1 = 0; j1 < achunksection.length; ++j1) {
            if (achunksection[j1] != null && (!flag || !achunksection[j1].a()) && (i & 1 << j1) != 0) {
                nibblearray = achunksection[j1].k();
                System.arraycopy(nibblearray.a, 0, abyte, i1, nibblearray.a.length);
                i1 += nibblearray.a.length;
            }
        }

        if (k > 0) {
            for (j1 = 0; j1 < achunksection.length; ++j1) {
                if (achunksection[j1] != null && (!flag || !achunksection[j1].a()) && achunksection[j1].h() != null && (i & 1 << j1) != 0) {
                    nibblearray = achunksection[j1].h();
                    System.arraycopy(nibblearray.a, 0, abyte, i1, nibblearray.a.length);
                    i1 += nibblearray.a.length;
                }
            }
        }

        if (flag) {
            byte[] abyte2 = chunk.l();

            System.arraycopy(abyte2, 0, abyte, i1, abyte2.length);
            i1 += abyte2.length;
        }

        /* CraftBukkit start - Moved compression into its own method.
        byte[] abyte = data; // CraftBukkit - uses data from above constructor
        Deflater deflater = new Deflater(-1);

        try {
            deflater.setInput(abyte, 0, i1);
            deflater.finish();
            this.buffer = new byte[i1];
            this.size = deflater.deflate(this.buffer);
        } finally {
            deflater.end();
        } */
        this.rawData = abyte;
        // CraftBukkit end
    }

    public void a(DataInputStream datainputstream) throws IOException { // CraftBukkit - throws IOEXception
        this.a = datainputstream.readInt();
        this.b = datainputstream.readInt();
        this.f = datainputstream.readBoolean();
        this.c = datainputstream.readShort();
        this.d = datainputstream.readShort();
        this.size = datainputstream.readInt();
        this.h = datainputstream.readInt();
        if (rawData.length < this.size) {
            rawData = new byte[this.size];
        }

        datainputstream.readFully(rawData, 0, this.size);
        int i = 0;

        int j;

        for (j = 0; j < 16; ++j) {
            i += this.c >> j & 1;
        }

        j = 12288 * i;
        if (this.f) {
            j += 256;
        }

        this.buffer = new byte[j];
        Inflater inflater = new Inflater();

        inflater.setInput(rawData, 0, this.size);

        try {
            inflater.inflate(this.buffer);
        } catch (DataFormatException dataformatexception) {
            throw new IOException("Bad compressed data format");
        } finally {
            inflater.end();
        }
    }

    public void a(DataOutputStream dataoutputstream) throws IOException { // CraftBukkit - throws IOException
        dataoutputstream.writeInt(this.a);
        dataoutputstream.writeInt(this.b);
        dataoutputstream.writeBoolean(this.f);
        dataoutputstream.writeShort((short) (this.c & '\uffff'));
        dataoutputstream.writeShort((short) (this.d & '\uffff'));
        dataoutputstream.writeInt(this.size);
        dataoutputstream.writeInt(this.h);
        dataoutputstream.write(this.buffer, 0, this.size);
    }

    public void handle(NetHandler nethandler) {
        nethandler.a(this);
    }

    public int a() {
        return 17 + this.size;
    }

    // begin Tyr
    public final void obfuscateandcompressPacket51MapChunk(World theworld, int[] specificworldconfig)
    {
        int enginemode = 0;
        byte maingroundblock = 1;
        boolean runchestlogic = false;
        if (specificworldconfig == null) {
            enginemode = OreObfusicationEngine;
            maingroundblock = maingroundblock;
            runchestlogic = runchestlogic;
        } else {
            enginemode = specificworldconfig[1];
            maingroundblock = (byte)specificworldconfig[2];
            runchestlogic = specificworldconfig[3] == 1;
        }
        int worldoriginx = this.a * 16;
        int worldoriginz = this.b * 16;
        int worldoriginy = 0;
        int sizex = 16;
        int sizey = 0;
        int sizez = 16;

        for (int i = 0; i < 16; i++)
        {
            if ((this.c & 1 << i) <= 0)
                continue;
            sizey += 16;
        }

        byte[] original = new byte[this.rawData.length];
        System.arraycopy(this.rawData, 0, original, 0, this.rawData.length);

        if ((enginemode == 2) && 
            (sizey > 1))
        {
            for (int posx = 0; posx < sizex; posx++) {
                for (int posz = 0; posz < sizez; posz++) {
                    for (int posy = 0; posy < sizey; posy++) {
                        int chunksection = posy >> 4;

                        int index = chunksection * 4096 + (posy % 16 << 8) + (posz << 4) + posx;

                        if ((this.rawData[index] != 56) && (this.rawData[index] != 14) && (this.rawData[index] != 99) && 
                            (this.rawData[index] != 15) && (this.rawData[index] != 16) && 
                            (this.rawData[index] != 73) && (this.rawData[index] != 21) && (
                            (this.rawData[index] != 54) || (!runchestlogic)))
                        {
                            continue;
                        }
                        if ((this.rawData[index] & 0xF) == 0) {
                            this.rawData[index] = maingroundblock;
                        }

                        int checkindexup = index;
                        int checkindexdown = index;
                        int checkindexnorth = index;
                        int checkindexeast = index;
                        int checkindexsouth = index;
                        int checkindexwest = index;

                        byte adjacentidx = -127;
                        byte adjacentidz = -127;
                        if (posy != 0) {
                            checkindexdown = (posy - 1 >> 4) * 4096 + ((posy - 1) % 16 << 8) + (posz << 4) + posx;
                        }

                        if (posy != sizey - 1) {
                            checkindexup = (posy + 1 >> 4) * 4096 + ((posy + 1) % 16 << 8) + (posz << 4) + posx;

                            if (posx != 0) {
                                checkindexnorth = chunksection * 4096 + (posy % 16 << 8) + (posz << 4) + (posx - 1);
                            }
                            else {
                                adjacentidx = (byte)theworld.getTypeId(posx - 1 + worldoriginx, posy + worldoriginy, posz + worldoriginz);
                            }
                            if (posx != sizex - 1) {
                                checkindexsouth = chunksection * 4096 + (posy % 16 << 8) + (posz << 4) + (posx + 1);
                            }
                            else {
                                adjacentidx = (byte)theworld.getTypeId(posx + 1 + worldoriginx, posy + worldoriginy, posz + worldoriginz);
                            }
                            if (posz != 0) {
                                checkindexeast = chunksection * 4096 + (posy % 16 << 8) + (posz - 1 << 4) + posx;
                            }
                            else {
                                adjacentidz = (byte)theworld.getTypeId(posx + worldoriginx, posy + worldoriginy, posz - 1 + worldoriginz);
                            }
                            if (posz != sizez - 1) {
                                checkindexwest = chunksection * 4096 + (posy % 16 << 8) + (posz + 1 << 4) + posx;
                            }
                            else {
                                adjacentidz = (byte)theworld.getTypeId(posx + worldoriginx, posy + worldoriginy, posz + 1 + worldoriginz);
                            }

                            if ((isTransparentId(this.rawData[checkindexup])) || 
                                (isTransparentId(this.rawData[checkindexdown])) || 
                                (isTransparentId(this.rawData[checkindexnorth])) || 
                                (isTransparentId(this.rawData[checkindexeast])) || 
                                (isTransparentId(this.rawData[checkindexsouth])) || 
                                (isTransparentId(this.rawData[checkindexwest])) || 
                                (isTransparentId(adjacentidx)) || 
                                (isTransparentId(adjacentidz))) {
                                continue;
                            }
                            this.rawData[index] = maingroundblock;
                        }
                    }
                }
            }

        }

        if (enginemode == 3)
        {
            if (sizey > 1) {
                for (int posx = 0; posx < sizex; posx++) {
                    for (int posz = 0; posz < sizez; posz++) {
                        for (int posy = 0; posy < sizey; posy++) {
                            int index = (posy >> 4) * 4096 + (posy % 16 << 8) + (posz << 4) + posx;
                            if ((this.rawData[index] != 56) && (this.rawData[index] != 14) && (this.rawData[index] != 99) && 
                                (this.rawData[index] != 15) && (this.rawData[index] != 16) && (this.rawData[index] != 99) && 
                                (this.rawData[index] != 73) && (this.rawData[index] != 21)) continue;
                            this.rawData[index] = maingroundblock;
                        }
                    }
                }

                if (runchestlogic)
                {
                    for (int posx = 0; posx < sizex; posx++) {
                        for (int posz = 0; posz < sizez; posz++) {
                            for (int posy = 0; posy < sizey; posy++) {
                                int chunksection = posy >> 4;
                                int index = (posy >> 4) * 4096 + (posy % 16 << 8) + (posz << 4) + posx;
                                int checkindexup = index;
                                int checkindexdown = index;
                                int checkindexnorth = index;
                                int checkindexeast = index;
                                int checkindexsouth = index;
                                int checkindexwest = index;
                                if (this.rawData[index] == 54) {
                                    byte adjacentidx = -127;
                                    byte adjacentidz = -127;
                                    byte adjacentidy = -127;
                                    if (posy != 0) {
                                        checkindexdown = (posy - 1 >> 4) * 4096 + ((posy - 1) % 16 << 8) + (posz << 4) + posx;
                                    }

                                    if (posy != sizey - 1) {
                                        checkindexup = (posy + 1 >> 4) * 4096 + ((posy + 1) % 16 << 8) + (posz << 4) + posx;

                                        if (posx != 0) {
                                            checkindexnorth = chunksection * 4096 + (posy % 16 << 8) + (posz << 4) + (posx - 1);
                                        }
                                        else {
                                            adjacentidx = (byte)theworld.getTypeId(posx - 1 + worldoriginx, posy + worldoriginy, posz + worldoriginz);
                                        }
                                        if (posx != sizex - 1) {
                                            checkindexsouth = chunksection * 4096 + (posy % 16 << 8) + (posz << 4) + (posx + 1);
                                        }
                                        else {
                                            adjacentidx = (byte)theworld.getTypeId(posx + 1 + worldoriginx, posy + worldoriginy, posz + worldoriginz);
                                        }
                                        if (posz != 0) {
                                            checkindexeast = chunksection * 4096 + (posy % 16 << 8) + (posz - 1 << 4) + posx;
                                        }
                                        else {
                                            adjacentidz = (byte)theworld.getTypeId(posx + worldoriginx, posy + worldoriginy, posz - 1 + worldoriginz);
                                        }
                                        if (posz != sizez - 1) {
                                            checkindexwest = chunksection * 4096 + (posy % 16 << 8) + (posz + 1 << 4) + posx;
                                        }
                                        else {
                                            adjacentidz = (byte)theworld.getTypeId(posx + worldoriginx, posy + worldoriginy, posz + 1 + worldoriginz);
                                        }

                                        if ((isTransparentId(this.rawData[checkindexup])) || 
                                            (isTransparentId(this.rawData[checkindexdown])) || 
                                            (isTransparentId(this.rawData[checkindexnorth])) || 
                                            (isTransparentId(this.rawData[checkindexeast])) || 
                                            (isTransparentId(this.rawData[checkindexsouth])) || 
                                            (isTransparentId(this.rawData[checkindexwest])) || 
                                            (isTransparentId(adjacentidx)) || 
                                            (isTransparentId(adjacentidy)) || 
                                            (isTransparentId(adjacentidz))) {
                                            continue;
                                        }
                                        this.rawData[index] = maingroundblock;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if ((enginemode == 4) && 
            (sizey > 1)) {
            for (int posx = 0; posx < sizex; posx++) {
                for (int posz = 0; posz < sizez; posz++) {
                    for (int posy = 0; posy < sizey; posy++) {
                        int chunksection = posy >> 4;
                        int index = chunksection * 4096 + (posy % 16 << 8) + (posz << 4) + posx;

                        if ((this.rawData[index] != 56) && (this.rawData[index] != 14) && (this.rawData[index] != 99) && 
                            (this.rawData[index] != 15) && 
                            (this.rawData[index] != 16) && 
                            (this.rawData[index] != 73) && 
                            (this.rawData[index] != 21) && (
                            (this.rawData[index] != 54) || (!runchestlogic)))
                        {
                            continue;
                        }
                        if ((this.rawData[index] & 0xF) == 0) {
                            this.rawData[index] = maingroundblock;
                        }
                    }
                }
            }

        }

        if (enginemode == 5)
        {
            if (sizey > 1) {
                for (int posx = 0; posx < sizex; posx++) {
                    for (int posz = 0; posz < sizez; posz++) {
                        for (int posy = 0; posy < sizey; posy++) {
                            int chunksection = posy >> 4;
                            int index = (posy >> 4) * 4096 + (posy % 16 << 8) + (posz << 4) + posx;
                            if ((this.rawData[index] != maingroundblock) && (this.rawData[index] != 56) && (this.rawData[index] != 14) && (this.rawData[index] != 99) && 
                                (this.rawData[index] != 15) && (this.rawData[index] != 16) && 
                                (this.rawData[index] != 73) && (this.rawData[index] != 21) && (
                                (this.rawData[index] != 54) || (!runchestlogic))) continue;
                            int checkindexup = index;
                            int checkindexdown = index;
                            int checkindexnorth = index;
                            int checkindexeast = index;
                            int checkindexsouth = index;
                            int checkindexwest = index;
                            int checkindexup2 = index;
                            int checkindexdown2 = index;
                            int checkindexnorth2 = index;
                            int checkindexeast2 = index;
                            int checkindexsouth2 = index;
                            int checkindexwest2 = index;
                            byte adjacentidx = -127;
                            byte adjacentidz = -127;
                            byte adjacentidy = -127;
                            byte adjacentidx2 = -127;
                            byte adjacentidz2 = -127;
                            byte adjacentidy2 = -127;
                            if (posy != 0) {
                                checkindexdown = (posy - 1 >> 4) * 4096 + ((posy - 1) % 16 << 8) + (posz << 4) + posx;
                            }

                            if (posy != sizey - 1) {
                                checkindexup = (posy + 1 >> 4) * 4096 + ((posy + 1) % 16 << 8) + (posz << 4) + posx;

                                if (posx != 0) {
                                    checkindexnorth = chunksection * 4096 + (posy % 16 << 8) + (posz << 4) + (posx - 1);
                                }
                                else {
                                    adjacentidx = (byte)theworld.getTypeId(posx - 1 + worldoriginx, posy + worldoriginy, posz + worldoriginz);
                                }
                                if (posx != sizex - 1) {
                                    checkindexsouth = chunksection * 4096 + (posy % 16 << 8) + (posz << 4) + (posx + 1);
                                }
                                else {
                                    adjacentidx = (byte)theworld.getTypeId(posx + 1 + worldoriginx, posy + worldoriginy, posz + worldoriginz);
                                }
                                if (posz != 0) {
                                    checkindexeast = chunksection * 4096 + (posy % 16 << 8) + (posz - 1 << 4) + posx;
                                }
                                else {
                                    adjacentidz = (byte)theworld.getTypeId(posx + worldoriginx, posy + worldoriginy, posz - 1 + worldoriginz);
                                }
                                if (posz != sizez - 1) {
                                    checkindexwest = chunksection * 4096 + (posy % 16 << 8) + (posz + 1 << 4) + posx;
                                }
                                else {
                                    adjacentidz = (byte)theworld.getTypeId(posx + worldoriginx, posy + worldoriginy, posz + 1 + worldoriginz);
                                }

                                if (posy != sizey - 2) {
                                    checkindexup2 = (posy + 2 >> 4) * 4096 + ((posy + 2) % 16 << 8) + (posz << 4) + posx;

                                    if ((posx != 0) && (posx != 1)) {
                                        checkindexnorth2 = chunksection * 4096 + (posy % 16 << 8) + (posz << 4) + (posx - 2);
                                    }
                                    else {
                                        adjacentidx2 = (byte)theworld.getTypeId(posx - 2 + worldoriginx, posy + worldoriginy, posz + worldoriginz);
                                    }
                                    if ((posx != sizex - 1) && (posx != sizex - 2)) {
                                        checkindexsouth2 = chunksection * 4096 + (posy % 16 << 8) + (posz << 4) + (posx + 2);
                                    }
                                    else {
                                        adjacentidx2 = (byte)theworld.getTypeId(posx + 2 + worldoriginx, posy + worldoriginy, posz + worldoriginz);
                                    }
                                    if ((posz != 0) && (posz != 1)) {
                                        checkindexeast2 = chunksection * 4096 + (posy % 16 << 8) + (posz - 2 << 4) + posx;
                                    }
                                    else {
                                        adjacentidz2 = (byte)theworld.getTypeId(posx + worldoriginx, posy + worldoriginy, posz - 2 + worldoriginz);
                                    }
                                    if ((posz != sizez - 1) && (posz != sizez - 2)) {
                                        checkindexwest2 = chunksection * 4096 + (posy % 16 << 8) + (posz + 2 << 4) + posx;
                                    }
                                    else {
                                        adjacentidz2 = (byte)theworld.getTypeId(posx + worldoriginx, posy + worldoriginy, posz + 2 + worldoriginz);
                                    }

                                    if (this.rawData[index] == 54)
                                    {
                                        if ((isTransparentId(this.rawData[checkindexup])) || 
                                            (isTransparentId(original[checkindexdown])) || 
                                            (isTransparentId(original[checkindexnorth])) || 
                                            (isTransparentId(original[checkindexeast])) || 
                                            (isTransparentId(original[checkindexsouth])) || 
                                            (isTransparentId(original[checkindexwest])) || 
                                            (isTransparentId(adjacentidx)) || 
                                            (isTransparentId(adjacentidz))) {
                                            continue;
                                        }
                                        this.rawData[index] = maingroundblock;
                                    }
                                    else
                                    {
                                        if ((isTransparentId(this.rawData[checkindexup])) || 
                                            (isTransparentId(this.rawData[checkindexdown])) || 
                                            (isTransparentId(this.rawData[checkindexnorth])) || 
                                            (isTransparentId(this.rawData[checkindexeast])) || 
                                            (isTransparentId(this.rawData[checkindexsouth])) || 
                                            (isTransparentId(this.rawData[checkindexwest])) || 
                                            (isTransparentId(adjacentidx)) || 
                                            (isTransparentId(adjacentidy)) || 
                                            (isTransparentId(adjacentidz)) || 
                                            (isTransparentId(this.rawData[checkindexup2])) || 
                                            (isTransparentId(this.rawData[checkindexdown2])) || 
                                            (isTransparentId(this.rawData[checkindexnorth2])) || 
                                            (isTransparentId(this.rawData[checkindexeast2])) || 
                                            (isTransparentId(this.rawData[checkindexsouth2])) || 
                                            (isTransparentId(this.rawData[checkindexwest2])) || 
                                            (isTransparentId(adjacentidx2)) || 
                                            (isTransparentId(adjacentidy2)) || 
                                            (isTransparentId(adjacentidz2))) {
                                            continue;
                                        }
                                        if (posy + worldoriginy > 20)
                                            this.rawData[index] = 15;
                                        else {
                                            this.rawData[index] = 56;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (enginemode == 6)
        {
            if (sizey > 1)
                for (int posx = 0; posx < sizex; posx++)
                    for (int posz = 0; posz < sizez; posz++)
                        for (int posy = 0; posy < sizey; posy++) {
                            int chunksection = posy >> 4;
                            int index = (posy >> 4) * 4096 + (posy % 16 << 8) + (posz << 4) + posx;
                            if ((this.rawData[index] != maingroundblock) && (this.rawData[index] != 56) && (this.rawData[index] != 14) && (this.rawData[index] != 99) && 
                                (this.rawData[index] != 15) && (this.rawData[index] != 16) && 
                                (this.rawData[index] != 73) && (this.rawData[index] != 21) && (
                                (this.rawData[index] != 54) || (!runchestlogic))) continue;
                            int checkindexup = index;
                            int checkindexdown = index;
                            int checkindexnorth = index;
                            int checkindexeast = index;
                            int checkindexsouth = index;
                            int checkindexwest = index;
                            int checkindexup2 = index;
                            int checkindexdown2 = index;
                            int checkindexnorth2 = index;
                            int checkindexeast2 = index;
                            int checkindexsouth2 = index;
                            int checkindexwest2 = index;
                            byte adjacentidx = -127;
                            byte adjacentidz = -127;
                            byte adjacentidy = -127;
                            byte adjacentidx2 = -127;
                            byte adjacentidz2 = -127;
                            byte adjacentidy2 = -127;
                            if (posy != 0) {
                                checkindexdown = (posy - 1 >> 4) * 4096 + ((posy - 1) % 16 << 8) + (posz << 4) + posx;
                            }

                            if (posy != sizey - 1) {
                                checkindexup = (posy + 2 >> 4) * 4096 + ((posy + 1) % 16 << 8) + (posz << 4) + posx;

                                if (posx != 0) {
                                    checkindexnorth = chunksection * 4096 + (posy % 16 << 8) + (posz << 4) + (posx - 1);
                                }
                                else {
                                    adjacentidx = (byte)theworld.getTypeId(posx - 1 + worldoriginx, posy + worldoriginy, posz + worldoriginz);
                                }
                                if (posx != sizex - 1) {
                                    checkindexsouth = chunksection * 4096 + (posy % 16 << 8) + (posz << 4) + (posx + 1);
                                }
                                else {
                                    adjacentidx = (byte)theworld.getTypeId(posx + 1 + worldoriginx, posy + worldoriginy, posz + worldoriginz);
                                }
                                if (posz != 0) {
                                    checkindexeast = chunksection * 4096 + (posy % 16 << 8) + (posz - 1 << 4) + posx;
                                }
                                else {
                                    adjacentidz = (byte)theworld.getTypeId(posx + worldoriginx, posy + worldoriginy, posz - 1 + worldoriginz);
                                }
                                if (posz != sizez - 1) {
                                    checkindexwest = chunksection * 4096 + (posy % 16 << 8) + (posz + 1 << 4) + posx;
                                }
                                else {
                                    adjacentidz = (byte)theworld.getTypeId(posx + worldoriginx, posy + worldoriginy, posz + 1 + worldoriginz);
                                }

                                if (posy != sizey - 2) {
                                    checkindexup2 = (posy + 2 >> 4) * 4096 + ((posy + 1) % 16 << 8) + (posz << 4) + posx;

                                    if ((posx != 0) && (posx != 1)) {
                                        checkindexnorth2 = chunksection * 4096 + (posy % 16 << 8) + (posz << 4) + (posx - 2);
                                    }
                                    else {
                                        adjacentidx2 = (byte)theworld.getTypeId(posx - 2 + worldoriginx, posy + worldoriginy, posz + worldoriginz);
                                    }
                                    if ((posx != sizex - 1) && (posx != sizex - 2)) {
                                        checkindexsouth2 = chunksection * 4096 + (posy % 16 << 8) + (posz << 4) + (posx + 2);
                                    }
                                    else {
                                        adjacentidx2 = (byte)theworld.getTypeId(posx + 2 + worldoriginx, posy + worldoriginy, posz + worldoriginz);
                                    }
                                    if ((posz != 0) && (posz != 1)) {
                                        checkindexeast2 = chunksection * 4096 + (posy % 16 << 8) + (posz - 2 << 4) + posx;
                                    }
                                    else {
                                        adjacentidz2 = (byte)theworld.getTypeId(posx + worldoriginx, posy + worldoriginy, posz - 2 + worldoriginz);
                                    }
                                    if ((posz != sizez - 1) && (posz != sizez - 2)) {
                                        checkindexwest2 = chunksection * 4096 + (posy % 16 << 8) + (posz + 2 << 4) + posx;
                                    }
                                    else {
                                        adjacentidz2 = (byte)theworld.getTypeId(posx + worldoriginx, posy + worldoriginy, posz + 2 + worldoriginz);
                                    }

                                    if (this.rawData[index] == 54)
                                    {
                                        if ((isTransparentId(this.rawData[checkindexup])) || 
                                            (isTransparentId(original[checkindexdown])) || 
                                            (isTransparentId(original[checkindexnorth])) || 
                                            (isTransparentId(original[checkindexeast])) || 
                                            (isTransparentId(original[checkindexsouth])) || 
                                            (isTransparentId(original[checkindexwest])) || 
                                            (isTransparentId(adjacentidx)) || 
                                            (isTransparentId(adjacentidz))) {
                                            continue;
                                        }
                                        this.rawData[index] = fakeBlocks[obRand.nextInt(numFakeBlocks)];
                                    }
                                    else
                                    {
                                        if ((isTransparentId(this.rawData[checkindexup])) || 
                                            (isTransparentId(this.rawData[checkindexdown])) || 
                                            (isTransparentId(this.rawData[checkindexnorth])) || 
                                            (isTransparentId(this.rawData[checkindexeast])) || 
                                            (isTransparentId(this.rawData[checkindexsouth])) || 
                                            (isTransparentId(this.rawData[checkindexwest])) || 
                                            (isTransparentId(adjacentidx)) || 
                                            (isTransparentId(adjacentidy)) || 
                                            (isTransparentId(adjacentidz)) || 
                                            (isTransparentId(this.rawData[checkindexup2])) || 
                                            (isTransparentId(this.rawData[checkindexdown2])) || 
                                            (isTransparentId(this.rawData[checkindexnorth2])) || 
                                            (isTransparentId(this.rawData[checkindexeast2])) || 
                                            (isTransparentId(this.rawData[checkindexsouth2])) || 
                                            (isTransparentId(this.rawData[checkindexwest2])) || 
                                            (isTransparentId(adjacentidx2)) || 
                                            (isTransparentId(adjacentidy2)) || 
                                            (isTransparentId(adjacentidz2))) {
                                            continue;
                                        }
                                        this.rawData[index] = fakeBlocks[obRand.nextInt(numFakeBlocks)];
                                    }
                                }
                            }
                        }
        }
    }

    private final boolean isTransparentId(byte id)
    {
        if (id == 0) return true;
        if (id == -127) return false;
        for (byte index = 0; index < transparentIds.length; index = (byte)(index + 1)) {
            if (id == transparentIds[index]) return true;
        }
        return false;
    }
    // end Tyr
}
