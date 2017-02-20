package org.kans.zxb.service;

import org.kans.zxb.service.IKServiceBranch;

public class TestServiceItem implements IKServiceBranch {

	@Override
	public void init(KServiceMain mKService) {

	}

	@Override
	public boolean isLive() {
		return false;
	}


}
