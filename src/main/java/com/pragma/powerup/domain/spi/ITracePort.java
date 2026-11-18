package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.OrderTraceModel;

/** Puerto de salida hacia el microservicio de Trazabilidad. */
public interface ITracePort {

    /** Lanza TraceUnavailableException si trazabilidad-ms no responde. */
    void record(OrderTraceModel trace);
}
