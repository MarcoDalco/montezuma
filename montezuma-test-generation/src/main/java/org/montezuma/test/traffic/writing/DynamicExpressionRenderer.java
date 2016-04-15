package org.montezuma.test.traffic.writing;

public interface DynamicExpressionRenderer extends ExpressionRenderer {

	public ExpressionRenderer [] getMasterRenderers();

	public void setRenderers(ExpressionRenderer [] replaceRenderers);

}
