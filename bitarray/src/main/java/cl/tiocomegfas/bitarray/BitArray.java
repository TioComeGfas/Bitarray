package cl.tiocomegfas.bitarray;

/**
 * @author Luis Gajardo, Miguel Romero y Fernando Santolaya
 * Basado en
 * http://stackoverflow.com/questions/15736626/java-how-to-create-and-manipulate-a-bit-array-with-length-of-10-million-bits
 */
public class BitArray {
    private static final int WORD_SIZE = 64;
    private final long length;
    protected long[] bits;

    /**
     * Crea un Array de bits.
     *
     * @param size es la cantidad de bits que tiene el ubiobio.cl.bitArray.BitArray this.
     */
    public BitArray(long size) {
        this.length = size;
        //se constuye un arreglo con ceil(length / word_size) bloques.
        bits = new long[(int) (size / WORD_SIZE + 1)];
        build();
    }

    /**
     * Permite conocer el valor 0 o 1 de la i-ésima posición del ubiobio.cl.bitArray.BitArray
     *
     * @param pos
     * @return
     */
    public boolean getBit(int pos) {
        if (pos < 0) throw new IndexOutOfBoundsException("pos < 0: " + pos);
        if (pos >= length) throw new IndexOutOfBoundsException("pos >= length():" + pos);

        return (bits[pos / WORD_SIZE] & (1L << (pos % WORD_SIZE))) != 0;
    }

    /**
     * Pone en 1 el bits de la posición @pos del arreglo
     *
     * @param pos
     */
    public void setBit(int pos) {
        if (pos < 0) throw new IndexOutOfBoundsException("pos < 0: " + pos);
        if (pos > length) throw new IndexOutOfBoundsException("pos >= length():" + pos);

        long block = bits[pos / WORD_SIZE];
        long mask = (long) (1L << (pos % WORD_SIZE));
        block |= mask;
        bits[pos / WORD_SIZE] = block;
    }

    /**
     * Pone en 0 o 1 dependiendo si b es falso o verdadero respectivamente
     * la posición pos del ubiobio.cl.bitArray.BitArray.
     *
     * @param pos
     * @param b
     */
    public void setBit(int pos, boolean b) {
        if (pos < 0) throw new IndexOutOfBoundsException("pos < 0: " + pos);
        if (pos > length) throw new IndexOutOfBoundsException("pos >= length():" + pos);

        long block = bits[pos / WORD_SIZE];
        long mask = (long) (1L << (pos % WORD_SIZE));
        if (b) {
            block |= mask;
        } else {
            block &= ~mask;
        }
        bits[pos / WORD_SIZE] = block;
    }

    /**
     * Pone en 0 el bits de la posición pos del ubiobio.cl.bitArray.BitArray
     *
     * @param pos
     */
    public void clearBit(int pos) {
        if (pos < 0) throw new IndexOutOfBoundsException("pos < 0: " + pos);
        if (pos > length) throw new IndexOutOfBoundsException("pos >= length():" + pos);

        long block = bits[pos / WORD_SIZE];
        long mask = (long) (1L << (pos % WORD_SIZE));
        block &= ~mask;
        bits[pos / WORD_SIZE] = block;
    }

    /**
     * Retorna la cantidad de bits en el ubiobio.cl.bitArray.BitArray.
     *
     * @return
     */
    public long length() {
        return length;
    }

    /**
     * Retorna el tamaño del ubiobio.cl.bitArray.BitArray en byte.
     *
     * @return
     */
    public long size() {
        //8 por variable this.length
        //4 por bits.length
        //8 por la referencia a bits (pensando en arquitectura de 64 bits, peor caso).

        return (bits.length * WORD_SIZE) / 8 + 8 + 4 + 8;
    }

    private void build(){
        setBit(0); //inserto el nodo ficticio
        setBit(2); //inserto el nodo raiz
        int posArray = 4;

        while (posArray < length) {
            long cantidadHijos = (int) Math.floor(Math.random() * (50 - 1 + 1) + 1);

            long diferencia = length - posArray;
            if(cantidadHijos > diferencia) cantidadHijos = diferencia;

            for(int i=0; i < cantidadHijos; i++){
                setBit(posArray);
                posArray++;
            }
            posArray++;
        }

        setBit((int)length - 1,false);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < length; i++) {
            out.append(getBit(i) ? "1" : "0");
        }
        return out.toString();
    }

}