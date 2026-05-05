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

    // 最简分数形式
    @Override
    public String toString() {
        if (denominator.equals(BigInteger.ONE)) {
            return numerator.toString();
        } else {
            return numerator + "/" + denominator;
        }
    }

    // 获取分子（只读）
    public BigInteger getNumerator() { return numerator; }
    public BigInteger getDenominator() { return denominator; }

    /**
     * 将有理数转换为十进制字符串，保留 digits 位小数（四舍五入）
     * @param digits 小数位数（≥0）
     * @return 十进制字符串，例如 "0.333" 或 "-1.5"
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

        // 无小数部分
        if (remainder.equals(BigInteger.ZERO)) {
            String intStr = intPart.toString();
            if (negative) intStr = "-" + intStr;
            if (digits == 0) return intStr;
            return intStr + "." + "0".repeat(digits);
        }

        // 计算 digits+1 位小数
        int[] digitsArr = new int[digits + 1];
        BigInteger r = remainder;
        for (int i = 0; i <= digits; i++) {
            r = r.multiply(BigInteger.TEN);
            digitsArr[i] = r.divide(den).intValue();
            r = r.remainder(den);
        }

        // 四舍五入
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

    // ========== 主方法：计算调和级数 H_n ==========
    public static void main(String[] args) {
        int n = 10;  // 可修改为更大的值（如 100），但分数会非常巨大
        BigRational sum = new BigRational("0");
        for (int k = 1; k <= n; k++) {
            sum = sum.add(new BigRational("1/" + k));
        }
        System.out.println("H_" + n + " 精确分数 = " + sum);
        System.out.println("H_" + n + " 十进制近似 = " + sum.toDecimal(10));
    }
}