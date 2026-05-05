import java.math.BigInteger;

public class BigRational {
    private final BigInteger numerator;
    private final BigInteger denominator;

    // 默认 0/1
    public BigRational() {
        this(BigInteger.ZERO, BigInteger.ONE);
    }

    // 从字符串构造（支持分数、整数、小数）
    public BigRational(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException("Input string is null or empty");
        }
        s = s.trim();
        if (s.contains("/")) {
            String[] parts = s.split("/");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid fraction format: " + s);
            }
            BigInteger num = new BigInteger(parts[0]);
            BigInteger den = new BigInteger(parts[1]);
            if (den.equals(BigInteger.ZERO)) {
                throw new IllegalArgumentException("Denominator cannot be zero");
            }
            BigRational temp = new BigRational(num, den);
            this.numerator = temp.numerator;
            this.denominator = temp.denominator;
        } else {
            BigRational parsed = parseDecimal(s);
            this.numerator = parsed.numerator;
            this.denominator = parsed.denominator;
        }
    }

    // 私有构造，直接规范化
    private BigRational(BigInteger num, BigInteger den) {
        if (den.equals(BigInteger.ZERO)) {
            throw new IllegalArgumentException("Denominator cannot be zero");
        }
        BigInteger g = num.gcd(den);
        num = num.divide(g);
        den = den.divide(g);
        if (den.compareTo(BigInteger.ZERO) < 0) {
            num = num.negate();
            den = den.negate();
        }
        this.numerator = num;
        this.denominator = den;
    }

    // 解析小数 "2.5", "-0.125", "0"
    private static BigRational parseDecimal(String s) {
        boolean negative = s.startsWith("-");
        String numStr = negative ? s.substring(1) : s;
        int dotIndex = numStr.indexOf('.');
        if (dotIndex < 0) {
            return new BigRational(new BigInteger(s), BigInteger.ONE);
        }
        String integerPart = numStr.substring(0, dotIndex);
        String fractionalPart = numStr.substring(dotIndex + 1);
        if (integerPart.isEmpty()) integerPart = "0";
        if (fractionalPart.isEmpty()) fractionalPart = "0";
        BigInteger intVal = new BigInteger(integerPart);
        BigInteger fracVal = new BigInteger(fractionalPart);
        int fracLen = fractionalPart.length();
        BigInteger den = BigInteger.TEN.pow(fracLen);
        BigInteger num = intVal.multiply(den).add(fracVal);
        if (negative) num = num.negate();
        return new BigRational(num, den);
    }

    // 加法
    public BigRational add(BigRational x) {
        BigInteger newNum = this.numerator.multiply(x.denominator)
                .add(x.numerator.multiply(this.denominator));
        BigInteger newDen = this.denominator.multiply(x.denominator);
        return new BigRational(newNum, newDen);
    }

    // 减法
    public BigRational subtract(BigRational x) {
        BigInteger newNum = this.numerator.multiply(x.denominator)
                .subtract(x.numerator.multiply(this.denominator));
        BigInteger newDen = this.denominator.multiply(x.denominator);
        return new BigRational(newNum, newDen);
    }

    // 乘法
    public BigRational multiply(BigRational x) {
        BigInteger newNum = this.numerator.multiply(x.numerator);
        BigInteger newDen = this.denominator.multiply(x.denominator);
        return new BigRational(newNum, newDen);
    }

    // 除法
    public BigRational divide(BigRational x) {
        if (x.numerator.equals(BigInteger.ZERO)) {
            throw new ArithmeticException("Division by zero");
        }
        BigInteger newNum = this.numerator.multiply(x.denominator);
        BigInteger newDen = this.denominator.multiply(x.numerator);
        return new BigRational(newNum, newDen);
    }

    // 比较
    public int compare(BigRational x) {
        BigInteger left = this.numerator.multiply(x.denominator);
        BigInteger right = x.numerator.multiply(this.denominator);
        return left.compareTo(right);
    }

    // 相等
    public boolean equals(BigRational x) {
        if (x == null) return false;
        return compare(x) == 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BigRational)) return false;
        return equals((BigRational) obj);
    }

    @Override
    public int hashCode() {
        return numerator.hashCode() * 31 + denominator.hashCode();
    }

    @Override
    public String toString() {
        if (denominator.equals(BigInteger.ONE)) {
            return numerator.toString();
        } else {
            return numerator + "/" + denominator;
        }
    }

    public BigInteger getNumerator() { return numerator; }
    public BigInteger getDenominator() { return denominator; }

    /**
     * 将有理数转换为十进制字符串，保留 digits 位小数（四舍五入）
     */
    public String toDecimal(int digits) {
        if (digits < 0) {
            throw new IllegalArgumentException("digits must be non-negative");
        }
        if (numerator.equals(BigInteger.ZERO)) {
            if (digits == 0) return "0";
            return "0." + "0".repeat(digits);
        }
        boolean negative = numerator.compareTo(BigInteger.ZERO) < 0;
        BigInteger absNum = numerator.abs();
        BigInteger den = denominator;

        BigInteger intPart = absNum.divide(den);
        BigInteger remainder = absNum.remainder(den);

        if (remainder.equals(BigInteger.ZERO)) {
            String intStr = intPart.toString();
            if (negative) intStr = "-" + intStr;
            if (digits == 0) return intStr;
            return intStr + "." + "0".repeat(digits);
        }

        int[] digitsArr = new int[digits + 1];
        BigInteger r = remainder;
        for (int i = 0; i <= digits; i++) {
            r = r.multiply(BigInteger.TEN);
            digitsArr[i] = r.divide(den).intValue();
            r = r.remainder(den);
        }

        boolean carry = digitsArr[digits] >= 5;
        for (int i = digits - 1; i >= 0 && carry; i--) {
            digitsArr[i]++;
            if (digitsArr[i] == 10) {
                digitsArr[i] = 0;
                carry = true;
            } else {
                carry = false;
            }
        }
        if (carry) {
            intPart = intPart.add(BigInteger.ONE);
            for (int i = 0; i < digits; i++) {
                digitsArr[i] = 0;
            }
        }

        StringBuilder sb = new StringBuilder();
        if (negative) sb.append('-');
        sb.append(intPart);
        if (digits > 0) {
            sb.append('.');
            for (int i = 0; i < digits; i++) {
                sb.append(digitsArr[i]);
            }
        }
        return sb.toString();
    }

    // ========== 连分数工具方法 ==========
    /**
     * 根据连分数系数数组 [a0, a1, ..., ak] 计算所有收敛 C0..Ck
     * @param a 系数数组，a0 是整数，a1..ak 是正整数
     * @return 收敛的 BigRational 数组
     */
    public static BigRational[] convergents(int[] a) {
        int k = a.length - 1;
        BigRational[] conv = new BigRational[k + 1];
        // 递推公式: p_{-2}=0, p_{-1}=1; q_{-2}=1, q_{-1}=0
        BigInteger pPrev2 = BigInteger.ZERO;
        BigInteger pPrev1 = BigInteger.ONE;
        BigInteger qPrev2 = BigInteger.ONE;
        BigInteger qPrev1 = BigInteger.ZERO;
        for (int i = 0; i <= k; i++) {
            BigInteger ai = BigInteger.valueOf(a[i]);
            BigInteger p = ai.multiply(pPrev1).add(pPrev2);
            BigInteger q = ai.multiply(qPrev1).add(qPrev2);
            conv[i] = new BigRational(p, q);
            // 滑动
            pPrev2 = pPrev1;
            pPrev1 = p;
            qPrev2 = qPrev1;
            qPrev1 = q;
        }
        return conv;
    }

    /**
     * 生成 e 的连分数系数，长度为 n+1 (a0..an)
     * e = [2; 1,2,1,1,4,1,1,6,1,1,8,...]
     */
    public static int[] generateECoeff(int n) {
        int[] a = new int[n + 1];
        a[0] = 2;
        int k = 2;
        for (int i = 1; i <= n; i++) {
            if (i % 3 == 1) {
                a[i] = 1;
            } else if (i % 3 == 2) {
                a[i] = k;
                k += 2;
            } else { // i % 3 == 0
                a[i] = 1;
            }
        }
        return a;
    }

    // ========== 主方法：演示连分数逼近 ==========
    public static void main(String[] args) {
        // 1. √2 的连分数 [1;2,2,2,...]
        System.out.println("=== √2 的连分数收敛 ===");
        int sqrt2Depth = 5;  // 计算 C0 到 C5
        int[] sqrt2Coeff = new int[sqrt2Depth + 1];
        sqrt2Coeff[0] = 1;
        for (int i = 1; i <= sqrt2Depth; i++) {
            sqrt2Coeff[i] = 2;
        }
        BigRational[] sqrt2Conv = convergents(sqrt2Coeff);
        for (int i = 0; i <= sqrt2Depth; i++) {
            System.out.printf("C_%d = %-12s ≈ %s\n",
                    i, sqrt2Conv[i], sqrt2Conv[i].toDecimal(10));
        }

        // 2. e 的连分数 [2;1,2,1,1,4,1,1,6,...]
        System.out.println("\n=== e 的连分数收敛 ===");
        int eDepth = 8;  // 计算 C0 到 C8
        int[] eCoeff = generateECoeff(eDepth);
        BigRational[] eConv = convergents(eCoeff);
        for (int i = 0; i <= eDepth; i++) {
            System.out.printf("C_%d = %-12s ≈ %s\n",
                    i, eConv[i], eConv[i].toDecimal(10));
        }
    }
}