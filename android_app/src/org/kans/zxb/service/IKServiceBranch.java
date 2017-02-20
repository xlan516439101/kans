package org.kans.zxb.service;


public interface IKServiceBranch{
	public static final Class<?>[] itemsClasses = new Class<?>[]{
		TestServiceItem.class
	};
	abstract void init(KServiceMain mKService);
	abstract boolean isLive();
}