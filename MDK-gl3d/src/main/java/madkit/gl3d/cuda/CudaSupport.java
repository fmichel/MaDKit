package madkit.gl3d.cuda;

/** Optional CUDA capability detection; loading this class never initializes native CUDA. */
public final class CudaSupport {
    private static final String DISABLE_PROPERTY = "madkit.gl3d.cuda.disabled";
    private static final String DRIVER_CLASS = "jcuda.driver.JCudaDriver";

    private CudaSupport() {
    }

    /** Returns true only when JCuda's driver binding is present and CUDA was not disabled. */
    public static boolean isAvailable() {
        if (Boolean.getBoolean(DISABLE_PROPERTY)) {
            return false;
        }
        try {
            Class.forName(DRIVER_CLASS, false, CudaSupport.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError ignored) {
            return false;
        }
    }

    /** Returns a diagnostic suitable for a fallback message in an application. */
    public static String availabilityDescription() {
        if (Boolean.getBoolean(DISABLE_PROPERTY)) {
            return "CUDA disabled by -D" + DISABLE_PROPERTY;
        }
        return isAvailable() ? "JCuda driver binding detected" : "JCuda driver binding is not on the module path";
    }
}
