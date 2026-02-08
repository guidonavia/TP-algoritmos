package grafos;

public class Conexion {
    private Usuario origen;
    private Usuario destino;
    private int peso;

    public Conexion(Usuario origen, Usuario destino, int peso) {
        this.origen = origen;
        this.destino = destino;
        this.peso = peso;
    }

    public Usuario getOrigen() {
        return origen;
    }

    public Usuario getDestino() {
        return destino;
    }

    public int getPeso() {
        return peso;
    }


    @Override
    public String toString() {
        return origen + " --(" + peso + ")--> " + destino;
    }
}