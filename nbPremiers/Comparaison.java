package linda.nbPremiers;

public class Comparaison {
    
    public static void main(String[] args) {
        EratostheneSequentiel esq = new EratostheneSequentiel();
        EratostheneParallele epl = new EratostheneParallele();
        long startTime = System.nanoTime();
        esq.getPremiers(1000000);
        long stopTime = System.nanoTime();
        System.out.println("Temps séquentiel : " + (stopTime - startTime));
        startTime = System.nanoTime();
        epl.getPremiers(1000000);
        stopTime = System.nanoTime();
        System.out.println("Temps parallèle : " + (stopTime - startTime));
    }

}
