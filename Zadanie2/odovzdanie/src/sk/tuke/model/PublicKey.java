package sk.tuke.model;

import java.math.BigInteger;

public class PublicKey {
    private BigInteger e;
    private BigInteger modulus;

    public PublicKey(BigInteger e, BigInteger modulus) {
        this.e = e;
        this.modulus = modulus;
    }

    public BigInteger getE() {
        return e;
    }

    public BigInteger getModulus() {
        return modulus;
    }
}
