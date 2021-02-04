package cl.tiocomegfas.bitarray;

public class LoudsTree {
    private final RankSelect rankSelect;

    public LoudsTree(long size){
        rankSelect = new RankSelect(new BitArray(size));
    }

    /**
     * Retorna el padre de cada posicion
     * @param position
     * @return
     */
    public long parent(int position){
        return rankSelect.select0(rankSelect.rank0(rankSelect.select1(rankSelect.rank0(position)))) + 1;
    }

    /**
     * Retorna los hijos de cada posicion hacia abajo
     * @param position
     * @param indexChild
     * @return
     */
    public long child(int position, int indexChild){
        return rankSelect.select0(rankSelect.rank1(position + indexChild - 1)) + 1;
    }

    /**
     * Retorna el primer hijo
     * @param position
     * @return
     */
    public long firstChild(int position){
        return rankSelect.select0(rankSelect.rank1(position)) + 1;
    }

    /**
     * Retorna el ultimo hermano de la posicion
     * @param position
     * @return
     */
    public long nextSibling(int position){
        return rankSelect.select0(rankSelect.rank0(position) + 1) + 1;
    }

    /**
     * Retorna el bitarray entero
     * @return
     */
    public String printAll(){
        StringBuilder sb = new StringBuilder();
        return rankSelect.toString();
    }
}