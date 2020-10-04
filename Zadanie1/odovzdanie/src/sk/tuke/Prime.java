package sk.tuke;

public class Prime {

    public static int generatePrime(int n){
        int num = 1;
        for(int i = -1; i < n;){
            num++;
            if(isPrime(num)){
                i++;
            }
        }
        return num;
    }

    private static boolean isPrime(int num){
        for(int i = 2; i * i <= num; i++){
            if(num % i == 0){
                return false;
            }
        }
        return true;
    }
}
