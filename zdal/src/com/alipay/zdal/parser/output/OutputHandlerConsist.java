package com.alipay.zdal.parser.output;

import com.alipay.zdal.parser.sqlobjecttree.outputhandlerimpl.ChangePageNumber;
import com.alipay.zdal.parser.sqlobjecttree.outputhandlerimpl.ChangeTable;
import com.alipay.zdal.parser.sqlobjecttree.outputhandlerimpl.ChangeTableAndPageNumber;





public class OutputHandlerConsist {
	//TODO:ʹ��builderģʽ�ع��˴��߼�


	public final static OutputHandler CHANGE_TABLE_AND_PAGENUMBER = new ChangeTableAndPageNumber();



	public final static OutputHandler CHANGE_TABLE = new ChangeTable();

	public final static OutputHandler CHANGE_PAGENUMBER = new ChangePageNumber();
}
