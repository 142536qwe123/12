import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 完整粒子系统：fountain / explosion / smoke / spiral
 *
 * 用法:
 *   java ParticleSimulator <mode> <param> <dt> <T> [lambda]
 *
 * 参数说明:
 *   mode   : fountain | explosion | smoke | spiral
 *   param  : fountain/smoke/spiral -> 每秒发射粒子数
 *            explosion -> 总粒子数
 *   dt     : 固定时间步长（秒）
 *   T      : 运行时长（秒）
 *   lambda : 边界碰撞能量损失系数，默认 0.9
 */
public class ParticleSimulator {
    private static final double MIN_X = 0.0;
    private static final double MAX_X = 1.0;
    private static final double MIN_Y = 0.0;
    private static final double MAX_Y = 1.0;
    private static final double DEFAULT_LAMBDA = 0.9;

    private enum Mode {
        FOUNTAIN,
        EXPLOSION,
        SMOKE,
        SPIRAL
    }

    private static final class Particle {
        double x;
        double y;
        double vx;
        double vy;
        final double ax;
        final double ay;

        double life;
        final double maxLife;

        final double startRadius;
        final double endRadius;

        final int startR;
        final int startG;
        final int startB;
        final int endR;
        final int endG;
        final int endB;

        final double startAlpha;
        final double endAlpha;

        Particle(double x, double y,
                 double vx, double vy,
                 double ax, double ay,
                 double life,
                 double startRadius, double endRadius,
                 int startR, int startG, int startB,
                 int endR, int endG, int endB,
                 double startAlpha, double endAlpha) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.ax = ax;
            this.ay = ay;
            this.life = life;
            this.maxLife = life;
            this.startRadius = startRadius;
            this.endRadius = endRadius;
            this.startR = startR;
            this.startG = startG;
            this.startB = startB;
            this.endR = endR;
            this.endG = endG;
            this.endB = endB;
            this.startAlpha = startAlpha;
            this.endAlpha = endAlpha;
        }

        void update(double dt, double lambda) {
            if (!isAlive()) {
                return;
            }

            vx += ax * dt;
            vy += ay * dt;
            x += vx * dt;
            y += vy * dt;
            bounce(lambda);
            life -= dt;
        }

        private void bounce(double lambda) {
            for (int i = 0; i < 4; i++) {
                boolean changed = false;

                if (x < MIN_X) {
                    x = MIN_X + (MIN_X - x);
                    vx = -vx * lambda;
                    changed = true;
                } else if (x > MAX_X) {
                    x = MAX_X - (x - MAX_X);
                    vx = -vx * lambda;
                    changed = true;
                }

                if (y < MIN_Y) {
                    y = MIN_Y + (MIN_Y - y);
                    vy = -vy * lambda;
                    changed = true;
                } else if (y > MAX_Y) {
                    y = MAX_Y - (y - MAX_Y);
                    vy = -vy * lambda;
                    changed = true;
                }

                if (!changed) {
                    break;
                }
            }

            if (x < MIN_X) x = MIN_X;
            if (x > MAX_X) x = MAX_X;
            if (y < MIN_Y) y = MIN_Y;
            if (y > MAX_Y) y = MAX_Y;
        }

        boolean isAlive() {
            return life > 0.0;
        }

        void draw() {
            if (!isAlive()) {
                return;
            }

            double progress = 1.0 - life / maxLife;
            if (progress < 0.0) progress = 0.0;
            if (progress > 1.0) progress = 1.0;

            double radius = lerp(startRadius, endRadius, progress);
            double alpha = lerp(startAlpha, endAlpha, progress);

            int r = clampColor((int) Math.round(lerp(startR, endR, progress) * alpha));
            int g = clampColor((int) Math.round(lerp(startG, endG, progress) * alpha));
            int b = clampColor((int) Math.round(lerp(startB, endB, progress) * alpha));

            if (radius > 0.0 && alpha > 0.0) {
                StdDraw.setPenColor(r, g, b);
                StdDraw.filledCircle(x, y, radius);
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            printUsage();
            System.exit(1);
        }

        Mode mode = parseMode(args[0]);
        double param = parseDouble(args[1], "param");
        double dt = parsePositiveDouble(args[2], "dt");
        double totalTime = parsePositiveDouble(args[3], "T");
        double lambda = args.length >= 5 ? clamp(parseDouble(args[4], "lambda"), 0.0, 1.0) : DEFAULT_LAMBDA;

        StdDraw.setCanvasSize(800, 800);
        StdDraw.setXscale(MIN_X, MAX_X);
        StdDraw.setYscale(MIN_Y, MAX_Y);
        StdDraw.enableDoubleBuffering();

        List<Particle> particles = new ArrayList<>();
        double emitterX = 0.5;
        double emitterY = 0.2;
        double emitAccumulator = 0.0;
        boolean explosionTriggered = false;

        double elapsed = 0.0;
        while (elapsed < totalTime) {
            double stepDt = Math.min(dt, totalTime - elapsed);

            double[] emitter = syncEmitterWithMouse(emitterX, emitterY);
            emitterX = emitter[0];
            emitterY = emitter[1];

            if (mode == Mode.EXPLOSION) {
                if (!explosionTriggered) {
                    int totalParticles = Math.max(1, (int) Math.round(param));
                    for (int i = 0; i < totalParticles; i++) {
                        particles.add(createParticle(mode, emitterX, emitterY));
                    }
                    explosionTriggered = true;
                }
            } else {
                double rate = Math.max(0.0, param);
                emitAccumulator += rate * stepDt;
                int toEmit = (int) emitAccumulator;
                emitAccumulator -= toEmit;

                for (int i = 0; i < toEmit; i++) {
                    particles.add(createParticle(mode, emitterX, emitterY));
                }
            }

            Iterator<Particle> iterator = particles.iterator();
            while (iterator.hasNext()) {
                Particle particle = iterator.next();
                particle.update(stepDt, lambda);
                if (!particle.isAlive()) {
                    iterator.remove();
                }
            }

            StdDraw.clear(StdDraw.BLACK);
            drawEmitterMarker(emitterX, emitterY);
            for (Particle particle : particles) {
                particle.draw();
            }
            StdDraw.show();
            StdDraw.pause(Math.max(1, (int) Math.round(stepDt * 1000.0)));

            elapsed += stepDt;
        }
    }

    private static Particle createParticle(Mode mode, double sourceX, double sourceY) {
        switch (mode) {
            case FOUNTAIN:
                return createFountainParticle(sourceX, sourceY);
            case EXPLOSION:
                return createExplosionParticle(sourceX, sourceY);
            case SMOKE:
                return createSmokeParticle(sourceX, sourceY);
            case SPIRAL:
            default:
                return createSpiralParticle(sourceX, sourceY);
        }
    }

    private static Particle createFountainParticle(double sourceX, double sourceY) {
        double spawnX = clamp(sourceX + randomRange(-0.012, 0.012), MIN_X, MAX_X);
        double spawnY = clamp(sourceY + randomRange(0.0, 0.01), MIN_Y, MAX_Y);

        double fanAngle = randomRange(-Math.toRadians(28.0), Math.toRadians(28.0));
        double direction = Math.PI / 2.0 + fanAngle;
        double speed = randomRange(0.45, 0.80);

        double vx = speed * Math.cos(direction);
        double vy = speed * Math.sin(direction);
        double ax = 0.0;
        double ay = -1.05;
        double life = randomRange(1.5, 2.4);

        return new Particle(
                spawnX, spawnY,
                vx, vy,
                ax, ay,
                life,
                0.008, 0.004,
                90, 180, 255,
                235, 245, 255,
                1.0, 0.0
        );
    }

    private static Particle createExplosionParticle(double sourceX, double sourceY) {
        double angle = randomRange(0.0, Math.PI * 2.0);
        double speed = randomRange(0.55, 1.05);

        double vx = speed * Math.cos(angle);
        double vy = speed * Math.sin(angle);
        double ax = 0.0;
        double ay = -0.22;
        double life = randomRange(0.7, 1.2);

        return new Particle(
                sourceX, sourceY,
                vx, vy,
                ax, ay,
                life,
                0.010, 0.003,
                255, 230, 120,
                120, 30, 10,
                1.0, 0.0
        );
    }

    private static Particle createSmokeParticle(double sourceX, double sourceY) {
        double spawnX = clamp(sourceX + randomRange(-0.025, 0.025), MIN_X, MAX_X);
        double spawnY = clamp(sourceY + randomRange(-0.01, 0.015), MIN_Y, MAX_Y);

        double vx = randomRange(-0.035, 0.035);
        double vy = randomRange(0.03, 0.08);
        double ax = randomRange(-0.008, 0.008);
        double ay = 0.05;
        double life = randomRange(2.8, 4.2);

        return new Particle(
                spawnX, spawnY,
                vx, vy,
                ax, ay,
                life,
                0.012, 0.040,
                120, 120, 120,
                235, 235, 235,
                0.85, 0.0
        );
    }

    private static Particle createSpiralParticle(double sourceX, double sourceY) {
        double spawnRadius = randomRange(0.006, 0.020);
        double theta = randomRange(0.0, Math.PI * 2.0);
        double spawnX = clamp(sourceX + spawnRadius * Math.cos(theta), MIN_X, MAX_X);
        double spawnY = clamp(sourceY + spawnRadius * Math.sin(theta), MIN_Y, MAX_Y);

        double radialX = spawnX - sourceX;
        double radialY = spawnY - sourceY;
        double radialLength = Math.hypot(radialX, radialY);
        if (radialLength < 1e-8) {
            radialX = 1.0;
            radialY = 0.0;
            radialLength = 1.0;
        }

        radialX /= radialLength;
        radialY /= radialLength;

        double tangentX = -radialY;
        double tangentY = radialX;

        double inwardSpeed = randomRange(0.02, 0.08);
        double swirlSpeed = randomRange(0.18, 0.30);
        double vx = tangentX * swirlSpeed - radialX * inwardSpeed;
        double vy = tangentY * swirlSpeed - radialY * inwardSpeed;

        double radialPull = randomRange(0.45, 0.75);
        double swirlPull = randomRange(1.10, 1.60);
        double ax = -radialPull * radialX + swirlPull * tangentX;
        double ay = -radialPull * radialY + swirlPull * tangentY;
        double life = randomRange(2.2, 3.6);

        return new Particle(
                spawnX, spawnY,
                vx, vy,
                ax, ay,
                life,
                0.008, 0.020,
                80, 220, 255,
                255, 90, 210,
                1.0, 0.0
        );
    }

    private static void drawEmitterMarker(double x, double y) {
        StdDraw.setPenColor(255, 255, 255);
        StdDraw.filledCircle(x, y, 0.006);
    }

    private static double[] syncEmitterWithMouse(double currentX, double currentY) {
        double mouseX = StdDraw.mouseX();
        double mouseY = StdDraw.mouseY();
        if (isFinite(mouseX) && isFinite(mouseY) && isWithinCanvas(mouseX, mouseY)) {
            return new double[] { clamp(mouseX, MIN_X, MAX_X), clamp(mouseY, MIN_Y, MAX_Y) };
        }
        return new double[] { currentX, currentY };
    }

    private static boolean isWithinCanvas(double x, double y) {
        return x >= MIN_X && x <= MAX_X && y >= MIN_Y && y <= MAX_Y;
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    private static double randomRange(double min, double max) {
        return min + Math.random() * (max - min);
    }

    private static double lerp(double start, double end, double t) {
        return start + (end - start) * t;
    }

    private static double clamp(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    private static int clampColor(int value) {
        if (value < 0) return 0;
        if (value > 255) return 255;
        return value;
    }

    private static double parseDouble(String raw, String name) {
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException ex) {
            System.err.println(name + " 不是有效数字: " + raw);
            System.exit(1);
            return 0.0;
        }
    }

    private static double parsePositiveDouble(String raw, String name) {
        double value = parseDouble(raw, name);
        if (value <= 0.0) {
            System.err.println(name + " 必须大于 0: " + raw);
            System.exit(1);
        }
        return value;
    }

    private static Mode parseMode(String raw) {
        String value = raw.toLowerCase();
        switch (value) {
            case "fountain":
                return Mode.FOUNTAIN;
            case "explosion":
                return Mode.EXPLOSION;
            case "smoke":
                return Mode.SMOKE;
            case "spiral":
                return Mode.SPIRAL;
            default:
                System.err.println("未知模式: " + raw);
                printUsage();
                System.exit(1);
                return Mode.FOUNTAIN;
        }
    }

    private static void printUsage() {
        System.err.println("用法: java ParticleSimulator <mode> <param> <dt> <T> [lambda]");
        System.err.println("  mode   = fountain | explosion | smoke | spiral");
        System.err.println("  param  = fountain/smoke/spiral 的发射速率（粒子/秒），explosion 的总粒子数");
        System.err.println("  dt     = 固定时间步长（秒）");
        System.err.println("  T      = 运行时长（秒）");
        System.err.println("  lambda = 边界碰撞能量损失系数，默认 0.9");
    }
}