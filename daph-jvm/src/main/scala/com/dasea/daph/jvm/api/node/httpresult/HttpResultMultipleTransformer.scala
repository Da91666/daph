package com.dasea.daph.jvm.api.node.httpresult

import com.dasea.daph.api.node.base.transformer.MultipleTransformer
import com.dasea.daph.utils.HttpResult

abstract class HttpResultMultipleTransformer
  extends MultipleTransformer[HttpResult, HttpResult]
