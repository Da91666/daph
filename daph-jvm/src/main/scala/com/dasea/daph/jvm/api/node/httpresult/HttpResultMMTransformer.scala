package com.dasea.daph.jvm.api.node.httpresult

import com.dasea.daph.api.node.base.transformer.MMTransformer
import com.dasea.daph.utils.HttpResult

abstract class HttpResultMMTransformer
  extends MMTransformer[HttpResult, HttpResult]
