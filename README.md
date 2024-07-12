# Debit Card Microservice

Este microservicio se encarga de la administración de las tarjetas de débito y sus cuentas asociadas. Proporciona diversas operaciones para gestionar tarjetas de débito, asociar cuentas bancarias y realizar transacciones. Está registrado en un API Gateway y puede ser consumido desde la siguiente dirección: [http://4.152.240.150:8085/](http://4.152.240.150:8085/).

## Descripción del Proyecto

El microservicio de tarjetas de débito permite a los clientes asociar sus tarjetas de débito a todas las cuentas bancarias que posean y gestionar una cuenta principal desde la cual se aplicarán los retiros o pagos. En caso de que no haya saldo suficiente en la cuenta principal, se analizará la disponibilidad en las cuentas asociadas en el orden en que fueron vinculadas. A continuación se detallan los endpoints disponibles y su funcionalidad.

## Endpoints

### Crear una Tarjeta de Débito
- **Descripción**: Añadir una nueva tarjeta de débito al sistema.
- **Método**: `POST`
- **Ruta**: `http://4.152.240.150:8085/debit-cards`
- **Código de Respuesta**: 201 - Tarjeta de débito creada exitosamente.

### Asociar una Cuenta a una Tarjeta de Débito
- **Descripción**: Asociar una cuenta existente a una tarjeta de débito.
- **Método**: `PUT`
- **Ruta**: `http://4.152.240.150:8085/debit-cards/{cardId}/link-account/{accountId}`
- **Códigos de Respuesta**: 200 - Cuenta asociada exitosamente, 404 - Tarjeta de débito o cuenta no encontrada.

### Establecer la Cuenta Principal para una Tarjeta de Débito
- **Descripción**: Establecer la cuenta principal para una tarjeta de débito existente.
- **Método**: `PUT`
- **Ruta**: `http://4.152.240.150:8085/debit-cards/{cardId}/set-primary-account/{primaryAccountId}`
- **Códigos de Respuesta**: 200 - Cuenta principal establecida exitosamente, 404 - Tarjeta de débito no encontrada.

### Ver el Balance de la Cuenta Asociada Principal
- **Descripción**: Ver el balance de la cuenta principal asociada a una tarjeta de débito.
- **Método**: `GET`
- **Ruta**: `http://4.152.240.150:8085/debit-cards/balance/{cardId}`
- **Códigos de Respuesta**: 200 - Operación exitosa, 404 - Tarjeta de débito no encontrada.

### Obtener una Tarjeta de Débito por ID
- **Descripción**: Obtener los detalles de una tarjeta de débito por su ID.
- **Método**: `GET`
- **Ruta**: `http://4.152.240.150:8085/debit-cards/{id}`
- **Códigos de Respuesta**: 200 - Tarjeta de débito recuperada exitosamente, 404 - Tarjeta de débito no encontrada.

### Eliminar una Tarjeta de Débito
- **Descripción**: Eliminar una tarjeta de débito del sistema.
- **Método**: `DELETE`
- **Ruta**: `http://4.152.240.150:8085/debit-cards/{id}`
- **Códigos de Respuesta**: 200 - Tarjeta de débito eliminada exitosamente, 404 - Tarjeta de débito no encontrada.

### Depósito a una Tarjeta de Débito
- **Descripción**: Realizar un depósito a una tarjeta de débito.
- **Método**: `POST`
- **Ruta**: `http://4.152.240.150:8085/debit-cards/{id}/deposit`
- **Código de Respuesta**: 200 - Depósito exitoso.

### Retiro de una Tarjeta de Débito
- **Descripción**: Realizar un retiro de una tarjeta de débito.
- **Método**: `POST`
- **Ruta**: `http://4.152.240.150:8085/debit-cards/{id}/withdraw`
- **Código de Respuesta**: 200 - Retiro exitoso.

### Obtener Transacciones de una Tarjeta de Débito
- **Descripción**: Obtener todas las transacciones realizadas en una tarjeta de débito específica.
- **Método**: `GET`
- **Ruta**: `http://4.152.240.150:8085/debit-cards/{id}/transactions`
- **Códigos de Respuesta**: 200 - Operación exitosa, 404 - Tarjeta de débito no encontrada.

## Integración y Despliegue

Este microservicio está integrado dentro de un clúster de AKS (Azure Kubernetes Service) con integración continua. Cada commit se almacena en un registro y el despliegue se realiza de manera automática, garantizando que siempre esté disponible la versión más reciente y funcional del servicio.

## Información Adicional

Para ver más información de las peticiones, tanto body, request y response, revisar el contrato API en el recurso del proyecto.
