module madkit.simu.template {
	requires transitive madkit.base;
	exports madkit.simu.template;
	opens madkit.simu.template to madkit.base;
}