package madkit.agr;

/**
 * Implements Constants which are used for the primary CGR organization places.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 */
public class LocalCommunity {

	public static final String NAME = "local";

	/**
	 * Utility class
	 */
	private LocalCommunity() {
	}

	/**
	 * MDK kernel core groups.
	 */
	public static final class Groups {

		/**
		 * Utility class
		 */
		private Groups() {
			throw new IllegalStateException("Utility class");
		}

		/**
		 * The value of this constant is {@value}.
		 */
		static final String NETWORK = "network";
		/**
		 * The value of this constant is {@value}.
		 */
		public static final String SYSTEM = "system";
	}

	/**
	 * MDK kernel core roles. Default roles within a MaDKit organization.
	 * 
	 * @since MaDKit 5.0.0.10
	 */
	public static final class Roles {

		/**
		 * Utility class
		 */
		private Roles() {
			throw new IllegalStateException("Utility class");
		}

		/**
		 * The value of this constant is {@value}.
		 */
		public static final String KERNEL = "kernel";

		/**
		 * The value of this constant is {@value}.
		 */
		static final String NET_AGENT = "net agent";
		/**
		 * The value of this constant is {@value}.
		 */
		static final String UPDATER = "updater";

		/**
		 * The value of this constant is {@value}.
		 */
		static final String EMMITER = "emmiter";

	}

}
