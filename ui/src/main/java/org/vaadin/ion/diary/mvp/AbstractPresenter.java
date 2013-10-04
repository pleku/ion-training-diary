package org.vaadin.ion.diary.mvp;

public abstract class AbstractPresenter<P extends Presenter<P, V>, V extends View<P, V>>
		implements Presenter<P, V> {

	private static final long serialVersionUID = -6771995859044860526L;

	private V view;

	public void setView(V view) {
		this.view = view;
		init();
	}

	public V getView() {
		return view;
	}

	protected abstract void init();
}
