package fragrant.random;

import java.util.Random;

public class BedrockRandom extends Random {
    private static final int N = 624;
    private static final int M = 397;
    private static final int MATRIX_A = 0x9908b0df;
    private static final int UPPER_MASK = 0x80000000;
    private static final int LOWER_MASK = 0x7fffffff;
    private static final int[] MAG_01 = {0, MATRIX_A};
    private static final double TWO_POW_M32 = 1.0 / (1L << 32);
    private final int[] mt = new int[N];
    private int mti;
    private boolean haveNextNextGaussian;
    private float nextNextGaussian;
    private int mtiFast;
    private final boolean valid;

    public BedrockRandom(int seed) {
        valid = true;
        _setSeed(seed);
    }

    @Override
    public void setSeed(long seed) {
        if (valid)
            setSeed((int) seed);
    }

    public void setSeed(int seed) {
        _setSeed(seed);
    }

    @Override
    public int nextInt() {
        return _genRandInt32() >>> 1;
    }

    @Override
    public int nextInt(int bound) {
        if (bound > 0)
            return (int) (Integer.toUnsignedLong(_genRandInt32()) % bound);
        else
            return 0;
    }

    public int nextInt(int a, int b) {
        if (a < b)
            return a + nextInt(b - a);
        else
            return a;
    }

    @Override
    public boolean nextBoolean() {
        return (_genRandInt32() & 0x8000000) != 0;
    }

    @Override
    public float nextFloat() {
        return (float) _genRandReal2();
    }

    public float nextFloat(float bound) {
        return nextFloat() * bound;
    }

    public float nextFloat(float a, float b) {
        return a + (nextFloat() * (b - a));
    }

    public static double intToFloat(int x) {
        return (x & 0xFFFFFFFFL) * 2.328306436538696e-10;
    }

    @Override
    public double nextDouble() {
        return _genRandReal2();
    }

    @Override
    public double nextGaussian() {
        if (haveNextNextGaussian) {
            haveNextNextGaussian = false;
            return nextNextGaussian;
        }

        float v1, v2, s;
        do {
            v1 = nextFloat() * 2 - 1;
            v2 = nextFloat() * 2 - 1;
            s = v1 * v1 + v2 * v2;
        } while (s == 0 || s > 1);

        float multiplier = (float) Math.sqrt(-2 * (float) Math.log(s) / s);
        nextNextGaussian = v2 * multiplier;
        haveNextNextGaussian = true;
        return v1 * multiplier;
    }

    @Override
    protected int next(int bits) {
        return _genRandInt32() >>> (32 - bits);
    }

    private void _setSeed(int seed) {
        this.mti = N + 1;
        this.haveNextNextGaussian = false;
        this.nextNextGaussian = 0;
        _initGenRandFast(seed);
    }

    private void _initGenRand() {
        this.mt[0] = 5489;
        for (this.mti = 1; this.mti < N; this.mti++) {
            this.mt[mti] = 1812433253
                    * ((this.mt[this.mti - 1] >>> 30) ^ this.mt[this.mti - 1])
                    + this.mti;
        }
        this.mtiFast = N;
    }

    private void _initGenRandFast(int initialValue) {
        this.mt[0] = initialValue;
        for (this.mtiFast = 1; this.mtiFast <= M; this.mtiFast++) {
            this.mt[this.mtiFast] = 1812433253
                    * ((this.mt[this.mtiFast - 1] >>> 30) ^ this.mt[this.mtiFast - 1])
                    + this.mtiFast;
        }
        this.mti = N;
    }

    private int _genRandInt32() {
        if (this.mti == N) {
            this.mti = 0;
        } else if (this.mti > N) {
            _initGenRand();
            this.mti = 0;
        }

        if (this.mti >= N - M) {
            if (this.mti == N - 1) {
                this.mt[N - 1] = MAG_01[this.mt[0] & 1]
                        ^ ((this.mt[0] & LOWER_MASK | this.mt[N - 1] & UPPER_MASK) >>> 1)
                        ^ this.mt[M - 1];
            } else {
                this.mt[this.mti] = MAG_01[this.mt[this.mti + 1] & 1]
                        ^ ((this.mt[this.mti + 1] & LOWER_MASK | this.mt[this.mti] & UPPER_MASK) >>> 1)
                        ^ this.mt[this.mti - (N - M)];
            }
        } else {
            this.mt[this.mti] = MAG_01[this.mt[this.mti + 1] & 1]
                    ^ ((this.mt[this.mti + 1] & LOWER_MASK | this.mt[this.mti] & UPPER_MASK) >>> 1)
                    ^ this.mt[this.mti + M];

            if (this.mtiFast < N) {
                this.mt[this.mtiFast] = 1812433253
                        * ((this.mt[this.mtiFast - 1] >>> 30) ^ this.mt[this.mtiFast - 1])
                        + this.mtiFast;
                this.mtiFast++;
            }
        }

        int ret = this.mt[this.mti++];
        ret = ((ret ^ (ret >>> 11)) << 7) & 0x9d2c5680 ^ ret ^ (ret >>> 11);
        ret = (ret << 15) & 0xefc60000 ^ ret ^ (((ret << 15) & 0xefc60000 ^ ret) >>> 18);
        return ret;
    }

    private double _genRandReal2() {
        return Integer.toUnsignedLong(_genRandInt32()) * TWO_POW_M32;
    }

    public static int[] genNums(int seed, int n) {
        int[] mt = new int[n];
        int[] state = new int[n + 397];
        state[0] = seed;

        for (int i = 1; i < state.length; i++) {
            state[i] = (int) ((0xFFFFFFFFL & (0x6C078965 * (state[i - 1] ^ (state[i - 1] >>> 30)))) + i);
        }

        for (int i = 0; i < n; i++) {
            int y = (state[i] & 0x80000000) | (state[i + 1] & 0x7fffffff);
            mt[i] = (y >>> 1) ^ ((y & 1) == 0 ? 0 : 0x9908B0DF) ^ state[i + 397];
        }

        for (int i = 0; i < n; i++) {
            int y = mt[i];
            y ^= y >>> 11;
            y ^= (y << 7) & 0x9D2C5680;
            y ^= (y << 15) & 0xEFC60000;
            mt[i] = y ^ (y >>> 18);
        }

        return mt;
    }

    public static int uMod(int a, int n) {
        return (int) ((a & 0xFFFFFFFFL) % n);
    }
}