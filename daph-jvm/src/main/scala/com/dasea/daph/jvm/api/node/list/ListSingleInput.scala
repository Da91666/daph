package com.dasea.daph.jvm.api.node.list

import com.dasea.daph.api.node.base.connector.input.SingleInput

abstract class ListSingleInput[OUT]
  extends SingleInput[List[OUT]]
