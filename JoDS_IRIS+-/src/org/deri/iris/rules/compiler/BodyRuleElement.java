package org.deri.iris.rules.compiler;

import org.deri.iris.api.basics.IPredicate;

public abstract class BodyRuleElement extends RuleElement {

    public abstract View getView();

    public abstract IPredicate getPredicate();

}
