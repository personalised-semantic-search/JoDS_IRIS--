/* This file was generated by SableCC (http://www.sablecc.org/). */

package org.deri.iris.parser.node;

import org.deri.iris.parser.analysis.Analysis;

@SuppressWarnings("nls")
public final class TTComment extends Token
{
    public TTComment(String text)
    {
        setText(text);
    }

    public TTComment(String text, int line, int pos)
    {
        setText(text);
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TTComment(getText(), getLine(), getPos());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTTComment(this);
    }
}