package com.dasea.daph.jvm.api.node.list

import com.dasea.daph.api.node.base.transformer.SingleTransformer

abstract class ListSingleTransformer[IN, OUT]
  extends SingleTransformer[List[IN], List[OUT]]
