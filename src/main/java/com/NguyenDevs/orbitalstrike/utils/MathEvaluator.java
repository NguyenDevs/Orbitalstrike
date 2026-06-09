package com.NguyenDevs.orbitalstrike.utils;

public class MathEvaluator {
    public interface Expression {
        double evaluate(double t);
    }

    public static Expression parse(final String input) {
        final String str = input == null ? "0" : input.toLowerCase();
        try {
            return new Object() {
                int pos = -1, ch;

                void nextChar() {
                    ch = (++pos < str.length()) ? str.charAt(pos) : -1;
                }

                boolean eat(int charToEat) {
                    while (ch == ' ') nextChar();
                    if (ch == charToEat) {
                        nextChar();
                        return true;
                    }
                    return false;
                }

                Expression parse() {
                    nextChar();
                    Expression x = parseExpression();
                    if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                    return x;
                }

                Expression parseExpression() {
                    Expression x = parseTerm();
                    for (;;) {
                        if (eat('+')) {
                            Expression a = x, b = parseTerm();
                            x = t -> a.evaluate(t) + b.evaluate(t);
                        } else if (eat('-')) {
                            Expression a = x, b = parseTerm();
                            x = t -> a.evaluate(t) - b.evaluate(t);
                        } else return x;
                    }
                }

                Expression parseTerm() {
                    Expression x = parseFactor();
                    for (;;) {
                        if (eat('*')) {
                            Expression a = x, b = parseFactor();
                            x = t -> a.evaluate(t) * b.evaluate(t);
                        } else if (eat('/')) {
                            Expression a = x, b = parseFactor();
                            x = t -> a.evaluate(t) / b.evaluate(t);
                        } else if (eat('%')) {
                            Expression a = x, b = parseFactor();
                            x = t -> a.evaluate(t) % b.evaluate(t);
                        } else return x;
                    }
                }

                Expression parseFactor() {
                    if (eat('+')) return parseFactor();
                    if (eat('-')) {
                        Expression a = parseFactor();
                        return t -> -a.evaluate(t);
                    }

                    Expression x;
                    int startPos = this.pos;
                    if (eat('(')) {
                        x = parseExpression();
                        eat(')');
                    } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                        while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                        double val = Double.parseDouble(str.substring(startPos, this.pos));
                        x = t -> val;
                    } else if (ch >= 'a' && ch <= 'z') {
                        while (ch >= 'a' && ch <= 'z') nextChar();
                        String func = str.substring(startPos, this.pos);
                        if (func.equals("t")) {
                            x = t -> t;
                        } else {
                            Expression a = parseFactor();
                            if (func.equals("sqrt")) x = t -> Math.sqrt(a.evaluate(t));
                            else if (func.equals("sin")) x = t -> Math.sin(a.evaluate(t));
                            else if (func.equals("cos")) x = t -> Math.cos(a.evaluate(t));
                            else if (func.equals("tan")) x = t -> Math.tan(a.evaluate(t));
                            else if (func.equals("abs")) x = t -> Math.abs(a.evaluate(t));
                            else throw new RuntimeException("Unknown function: " + func);
                        }
                    } else {
                        throw new RuntimeException("Unexpected: " + (char) ch);
                    }

                    if (eat('^')) {
                        Expression a = x, b = parseFactor();
                        x = t -> Math.pow(a.evaluate(t), b.evaluate(t));
                    }
                    return x;
                }
            }.parse();
        } catch (Exception e) {
            return t -> {
                if (t < 0) java.util.logging.Logger.getLogger("OrbitalStrike").warning(
                    "MathEvaluator: Parse error for expression '" + input + "': " + e.getMessage()
                );
                return 0;
            };
        }
    }
}
