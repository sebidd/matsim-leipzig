package org.matsim.run.custom;

import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;

public class LocalAreaModule extends AbstractModule {

	public LocalAreaModule(Controler controler, String path){
		if(LocalAreaEngine.get() == null) LocalAreaEngine.create(path);
	}

	@Override
	public void install() {
		addControlerListenerBinding().toInstance(LocalAreaEngine.get());
	}
	
}
