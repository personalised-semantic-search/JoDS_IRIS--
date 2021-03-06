/* This file was generated by SableCC (http://www.sablecc.org/). */

package org.deri.iris.parser.node;

import org.deri.iris.parser.analysis.Analysis;

@SuppressWarnings("nls")
public final class TTPreFloat extends Token
{
    public TTPreFloat()
    {
        super.setText("_float");
    }

    public TTPreFloat(int line, int pos)
    {
        super.setText("_float");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TTPreFloat(getLine(), getPos());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTTPreFloat(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TTPreFloat text.");
    }
}
