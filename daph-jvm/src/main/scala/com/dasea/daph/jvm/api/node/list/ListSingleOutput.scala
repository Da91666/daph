package com.dasea.daph.jvm.api.node.list

import com.dasea.daph.api.node.base.connector.output.SingleOutput

abstract class ListSingleOutput[OUT]
  extends SingleOutput[List[OUT]]
