package org.vaadin.ion.diary.mvp;

import javax.annotation.PostConstruct;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CustomComponent;

public abstract class AbstractView<P extends Presenter<P, V>, V extends View<P, V>, L extends ComponentContainer>
		extends CustomComponent implements View<P, V> {

	private static final long serialVersionUID = 2551728845353529224L;

	public AbstractView(L layout) {
		setSizeFull();
		layout.setSizeFull();
		setCompositionRoot(layout);
	}

	@PostConstruct
	@SuppressWarnings("unchecked")
	protected void init() {
		getPresenter().setView((V) this);
	}

	public abstract P getPresenter();

	@SuppressWarnings("unchecked")
	public L getLayout() {
		return (L) getCompositionRoot();
	}

	protected void addComponent(Component component) {
		getLayout().addComponent(component);
	}
}
