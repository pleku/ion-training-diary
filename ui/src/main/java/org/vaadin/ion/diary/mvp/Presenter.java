package org.vaadin.ion.diary.mvp;

import java.io.Serializable;

public interface Presenter<P extends Presenter<P, V>, V extends View<P, V>>
		extends Serializable {

	public void setView(V view);

}
