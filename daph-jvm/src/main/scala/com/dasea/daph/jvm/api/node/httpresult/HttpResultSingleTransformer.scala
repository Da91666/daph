package com.dasea.daph.jvm.api.node.httpresult

import com.dasea.daph.api.node.base.transformer.SingleTransformer
import com.dasea.daph.utils.HttpResult

abstract class HttpResultSingleTransformer
  extends SingleTransformer[HttpResult, HttpResult]
