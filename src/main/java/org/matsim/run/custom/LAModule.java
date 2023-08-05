package org.matsim.run.custom;

import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;

public class LAModule extends AbstractModule {
	
	private LAHandler handler;
	
	
	public LAModule(Controler controler, String path){
		this.handler = new LAHandler(controler, this);
	}

	@Override
	public void install() {
		addEventHandlerBinding().toInstance(handler);
		addControlerListenerBinding().toInstance(handler);
	}
	
}
