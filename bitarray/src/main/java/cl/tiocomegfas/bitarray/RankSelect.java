package cl.tiocomegfas.bitarray;

/**
 * Grupo de investigación ALBA,
 * Proyecto 2030 ...
 *
 * @author Luis Gajardo, Miguel Romero y Fernando Santolaya
 * Basado en
 * BitSecuenceRG de libcds Autor Francisco Claude
 * https://github.com/fclaude/libcds/blob/master/src/static/bitsequence/BitSequenceRG.cpp
 */

public class RankSelect {
    private static final int WORD_SIZE = 64;
    /**
     * mask for obtaining the first 5 bits
     */
    private static final long mask63 = 63L;
    private final long length;
    private final int factor;
    private final int s;
    private final long ones;
    private final long[] bits;
    private long[] Rs; //arreglo de superBlock

    /**
     * Crea un Array de bits estático con soporte para las
     * operaciones de rank y select.
     *
     * @param ba
     */
    public RankSelect(BitArray ba) {
        this(ba, 20);
    }

    /**
     * Crea un Array de bits estático con soporte para las
     * operaciones de rank y select.
     *
     * @param ba,    bit array a clonar, sobre el cual se opera
     * @param factor factor con el cual se determina la redundancia de la estructura
     *               si factor=2, redundancia 50%
     *               factor=3, redundancia 33%
     *               factor=4, redundancia 25%
     *               factor=20, redundancia 5%;
     */
    public RankSelect(BitArray ba, int factor) {
        this.length = ba.length();
        bits = ba.bits.clone();
        this.factor = factor;
        if (factor == 0) factor = 20;
        s = WORD_SIZE * factor;
        buildRank();
        ones = rank1(length - 1);

    }

    private void buildRank() {
        int num_sblock = (int) (length / s);
        // +1 pues sumo la pos cero
        Rs = new long[num_sblock + 5];
        int j;
        Rs[0] = 0;
        for (j = 1; j <= num_sblock; j++) {
            Rs[j] = Rs[j - 1];
            Rs[j] += BuildRankSub((j - 1) * factor, factor);
        }
    }

    private long BuildRankSub(int ini, int bloques) {
        long rank = 0, aux;
        for (int i = ini; i < ini + bloques; i++) {
            if (i < bits.length) {
                aux = bits[i];
                rank += Long.bitCount(aux);
            }
        }
        return rank;             //retorna el numero de 1's del intervalo
    }

    public long numberOfOnes() {
        return ones;
    }

    /**
     * Permite conocer el valor 0 o 1 de la i-ésima posición del ubiobio.cl.bitArray.BitArray
     *
     * @param pos
     * @return
     */
    public boolean access(long pos) {
        if (pos < 0) throw new IndexOutOfBoundsException("pos < 0: " + pos);
        if (pos >= length) throw new IndexOutOfBoundsException("pos >= length():" + pos);
        return (bits[(int) (pos / WORD_SIZE)] & (1l << (pos % WORD_SIZE))) != 0;
    }

    /**
     * retorna la cantidad de unos que hay hasta pos inclusive.
     *
     * @param pos
     * @return
     * @throws IndexOutOfBoundsException, si pos esta fuera del rango [0..length)
     */
    public long rank1(long pos) {
        if (pos < 0) throw new IndexOutOfBoundsException("pos < 0: " + pos);
        if (pos >= length) throw new IndexOutOfBoundsException("pos >= length():" + pos);
        long i = pos + 1;
        int p = (int) (i / s);
        long resp = Rs[p];
        int aux = p * factor;
        for (int a = aux; a < i / WORD_SIZE; a++)
            resp += Long.bitCount(bits[a]);
        resp += Long.bitCount(bits[(int) (i / WORD_SIZE)] & ((1l << (i & mask63)) - 1l));
        return resp;
    }

    /**
     * retorna la posición en el bit array en la que ocurre el i-ésimo uno.
     *
     * @param i
     * @return
     */
    public long select1(long i) {
        long x = i;
        // returns i such that x=rank(i) && rank(i-1)<x or n if that i not exist
        // first binary search over first level rank structure
        // then sequential search using popcount over a int
        // then sequential search using popcount over a char
        // then sequential search bit a bit
        if (i <= 0) throw new IndexOutOfBoundsException("i <= 0: " + i);
        if (i > ones) throw new IndexOutOfBoundsException("i > amount of ones:" + i);
        //binary search over first level rank structure
        int l = 0, r = (int) (length / s);
        int mid = (l + r) / 2;
        long rankmid = Rs[mid];
        while (l <= r) {
            if (rankmid < x)
                l = mid + 1;
            else
                r = mid - 1;
            mid = (l + r) / 2;
            rankmid = Rs[mid];
        }
        //sequential search using popcount over a int
        int left;
        left = mid * factor;
        x -= rankmid;
        long j = bits[left];
        int onesJ = Long.bitCount(j);
        while (onesJ < x) {
            x -= onesJ;
            left++;
            if (left > bits.length) return length;
            j = bits[left];
            onesJ = Long.bitCount(j);
        }
        //sequential search using popcount over a char
        left = left * WORD_SIZE;
        rankmid = Long.bitCount(j);
        if (rankmid < x) {
            j = j >>> 8l;
            x -= rankmid;
            left += 8l;
            rankmid = Long.bitCount(j);
            if (rankmid < x) {
                j = j >>> 8l;
                x -= rankmid;
                left += 8l;
                rankmid = Long.bitCount(j);
                if (rankmid < x) {
                    j = j >>> 8l;
                    x -= rankmid;
                    left += 8l;
                }
            }
        }

        // then sequential search bit a bit
        while (x > 0) {
            if ((j & 1) > 0) x--;
            j = j >>> 1l;
            left++;
        }
        return left - 1;
    }

    /**
     * retorna la cantidad de ceros que hay hasta pos inclusive.
     *
     * @param pos posición hasta donde se quiere contar la cantidad de ceros.
     * @return total de ceros hasta la posicion pos
     */
    public long rank0(long pos) {
        return pos - rank1(pos) + 1;
    }

    /**
     * retorna la posición en el bit array en la que ocurre el i-ésimo cero.
     *
     * @param i número ordinal del cero buscado.
     * @return posición del i-esimo cero
     */
    public long select0(long i) {
        long x = i;
        // returns i such that x=rank_0(i) && rank_0(i-1)<x or exception if that i not exist
        // first binary search over first level rank structure
        // then sequential search using popcount over a int
        // then sequential search using popcount over a char
        // then sequential search bit a bit
        if (i <= 0) throw new IndexOutOfBoundsException("i < 1: " + i);
        if (i > length - ones) throw new IndexOutOfBoundsException("i > amount of 0:" + i);
        //binary search over first level rank structure
        if (x == 0) return 0;
        int l = 0, r = (int) (length / s);
        int mid = (l + r) / 2;
        long rankmid = mid * factor * WORD_SIZE - Rs[mid];
        while (l <= r) {
            if (rankmid < x)
                l = mid + 1;
            else
                r = mid - 1;
            mid = (l + r) / 2;
            rankmid = mid * factor * WORD_SIZE - Rs[mid];
        }
        //sequential search using popcount over a int
        int left;
        left = mid * factor;
        x -= rankmid;
        long j = bits[left];
        int zeros = WORD_SIZE - Long.bitCount(j);
        while (zeros < x) {
            x -= zeros;
            left++;
            if (left > bits.length) return length;
            j = bits[left];
            zeros = WORD_SIZE - Long.bitCount(j);
        }
        //sequential search using popcount over a char
        left = left * WORD_SIZE;
        rankmid = WORD_SIZE - Long.bitCount(j);
        if (rankmid < x) {
            j = j >> 8l;
            x -= rankmid;
            left += 8;
            rankmid = WORD_SIZE - Long.bitCount(j);
            if (rankmid < x) {
                j = j >> 8l;
                x -= rankmid;
                left += 8;
                rankmid = WORD_SIZE - Long.bitCount(j);
                if (rankmid < x) {
                    j = j >> 8l;
                    x -= rankmid;
                    left += 8;
                }
            }
        }

        // then sequential search bit a bit
        while (x > 0) {
            if (j % 2 == 0) x--;
            j = j >> 1l;
            left++;
        }
        left--;
        if (left > length) return length;
        return left;
    }

    /**
     * retorna el menor indice i>=start tal que access(i) es true;
     *
     * @param start
     * @return posición en el bitArray del 1 siguiente o igual a start.
     * En el caso de no haber un 1 posterior retorna el largo
     * en bits de la secuencia.
     */
    public long selectNext1(long start) {
        if (start < 0) throw new IndexOutOfBoundsException("start < 0: " + start);
        if (start >= length) throw new IndexOutOfBoundsException("start >= length:" + start);
        long count = start;
        long des;
        long aux2;
        des = (int) (count % WORD_SIZE);
        aux2 = bits[(int) (count / WORD_SIZE)] >>> des;
        if (aux2 != 0) {
            return count + Long.numberOfTrailingZeros(aux2);
        }

        for (int i = (int) (count / WORD_SIZE) + 1; i < bits.length; i++) {
            aux2 = bits[i];
            if (aux2 != 0) {
                return i * WORD_SIZE + Long.numberOfTrailingZeros(aux2);
            }
        }
        return length;
    }

    /**
     * retorna el mayor indice i<=start tal que access(i) es true;
     *
     * @param start
     * @return retorna la posición i del 1 previo a start.  Si no
     * hay un 1 previo a la posción start retorna  -1.
     */
    public long selectPrev1(long start) {
        // returns the position of the previous 1 bit before and including start.
        if (start < 0) throw new IndexOutOfBoundsException("start < 0: " + start);
        if (start >= length) throw new IndexOutOfBoundsException("start > length:" + start);
        if (start == 0) return -1;
        int i = (int) (start / WORD_SIZE);
        int offset = (int) (start % WORD_SIZE);
        //64 unos
        long mask = 0xffffffffffffffffL;
        long aux2 = bits[i] & (mask >>> (WORD_SIZE - offset));

        if (aux2 != 0) {
            return i * WORD_SIZE + 63 - Long.numberOfLeadingZeros(aux2);
        }
        for (int k = i - 1; k >= 0; k--) {
            aux2 = bits[k];
            if (aux2 != 0) {
                return k * WORD_SIZE + 63 - Long.numberOfLeadingZeros(aux2);
            }
        }
        return -1;
    }

    /**
     * retorna el menor indice i>start tal que access(i) es false;
     *
     * @param start
     * @return
     */
    public long selectNext0(long start) {
        if (start < 0) throw new IndexOutOfBoundsException("start < 0: " + start);
        if (start >= length) throw new IndexOutOfBoundsException("start >= length:" + start);
        long count = start;
        long des;
        long aux2;
        des = (int) (count % WORD_SIZE);
        aux2 = ~bits[(int) (count / WORD_SIZE)] >>> des;
        if (aux2 != 0) {
            return count + Long.numberOfTrailingZeros(aux2);
        }

        for (int i = (int) (count / WORD_SIZE) + 1; i < bits.length; i++) {
            aux2 = ~bits[i];
            if (aux2 != 0) {
                return i * WORD_SIZE + Long.numberOfTrailingZeros(aux2);
            }
        }
        return length;
    }

    /**
     * retorna el mayor indice i<start tal que access(i) es false;
     *
     * @param start
     * @return
     */
    public long selectPrev0(long start) {
        // returns the position of the previous 1 bit before and including start.
        if (start < 0) throw new IndexOutOfBoundsException("start < 0: " + start);
        if (start >= length) throw new IndexOutOfBoundsException("start > length:" + start);
        if (start == 0) return -1;
        int i = (int) (start / WORD_SIZE);
        long offset = (start % WORD_SIZE);
        //64 unos
        long mask = 0xffffffffffffffffL;
        long aux2 = ~bits[i] & (mask >>> (WORD_SIZE - offset));

        if (aux2 != 0) {
            return i * WORD_SIZE + 63 - Long.numberOfLeadingZeros(aux2);
        }
        for (int k = i - 1; k >= 0; k--) {
            aux2 = ~bits[k];
            if (aux2 != 0) {
                return k * WORD_SIZE + 63 - Long.numberOfLeadingZeros(aux2);
            }
        }
        return -1;
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
        long bitmapSize = (bits.length * WORD_SIZE) / 8 + 4;
        long sbSize = Rs.length * WORD_SIZE / 8 + 4;
        //variables:long: length, ones =2*8
        //int: factor y s =2*4
        //referencias a los arreglos (puntero): Rs, bits= 2*8 (word ram 64 bits)
        long otros = 8 + 8 + 4 + 4 + 8 + 8;
        return bitmapSize + sbSize + otros;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < length; i++) {
            out.append(access(i) ? "1" : "0");
        }
        return out.toString();
    }

    public long numberOfZeroes() {
        return length - ones;
    }

}