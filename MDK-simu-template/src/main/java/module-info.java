/** 
 * Provides a template for creating a simulation with MadKit.
 * <p>
 * For having the simulation working, The exported module should be opened to madkit.base.
 * 
 * <p>
 * 
*/
module madkit.simu.template {
	requires transitive madkit.base;
	exports madkit.simu.template;
	opens madkit.simu.template to madkit.base;
}