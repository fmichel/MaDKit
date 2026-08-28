/**
 * Deterministic, headless bee-colony domain and MaDKit composition.
 *
 * <p>{@link madkit.gl3d.bees.BeeColonyModel} is the authoritative CPU model.
 * Its immutable {@link madkit.gl3d.bees.ColonySnapshot} values cross into the
 * renderer through {@link madkit.gl3d.bees.ColonySnapshotExchange}; rendering
 * code never reads mutable bee or flower objects. Commands are applied by the
 * controller at tick boundaries, and seeded initialization makes fixed command
 * sequences reproducible.</p>
 *
 * <p>The documented world convention is a right-handed coordinate system with
 * positive Y up, the hive at the origin, and bounded flower coordinates around
 * the hive.</p>
 */
package madkit.gl3d.bees;
