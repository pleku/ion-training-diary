package com.vaadin.cdi;

import java.lang.annotation.Annotation;

public enum UIType {

	DESKTOP(DesktopUI.class), TABLET(TabletUI.class), PHONE(PhoneUI.class);

	private Class<? extends Annotation> annotation;

	UIType(Class<? extends Annotation> annotation) {
		this.annotation = annotation;
	}

	public Class<? extends Annotation> getAnnotation() {
		return annotation;
	}

}
