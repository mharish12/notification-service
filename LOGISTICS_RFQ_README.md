# Logistics RFQ System

A comprehensive logistics Request for Quotation (RFQ) system that manages the complete lifecycle of logistics demands, from initial demand creation to final transporter selection and pricing.

## System Overview

The logistics RFQ system uses a consolidated approach with a single `logistics_rfq` table that handles all RFQ types, differentiated by the `rfq_type` field. Relationships between different RFQ types are maintained through the `rfq_mapping` table.

### Flow Diagram

```
Demand RFQ (Ship From → Ship To)
    ↓
Segment RFQs (First Mile → Middle Mile → Last Mile)
    ↓
Requisition RFQs (Transporter Selection)
    ↓
Individual RFQs (Pricing from Multiple Transporters)
```

## Core Architecture

### 1. Consolidated LogisticsRFQ Entity

A single entity that handles all RFQ types with comprehensive fields:

**RFQ Types:**

- **DEMAND**: Initial demand RFQ with complete shipment details
- **SEGMENT**: Segment-specific RFQ for different transportation legs
- **REQUISITION**: Requisition for transporter selection

**Key Features:**

- Ship from/to locations with coordinates
- Cargo details (weight, volume, type, category)
- Truck requirements and special equipment
- Timeline (pickup/delivery dates)
- Budget and priority settings
- Transport modes and pricing models
- Provider requirements and certifications

### 2. RFQMapping Entity

Maintains relationships between different RFQ types:

**Relationship Types:**

- **DEMAND_TO_SEGMENT**: Links demand RFQ to segment RFQs
- **SEGMENT_TO_REQUISITION**: Links segment RFQ to requisition RFQs
- **REQUISITION_TO_RFQ**: Links requisition RFQ to individual pricing RFQs

## Database Schema

### Consolidated Table Structure

#### logistics_rfq

- **rfq_type**: DEMAND, SEGMENT, REQUISITION
- **Location Details**: Ship from/to addresses with coordinates
- **Cargo Details**: Weight, volume, type, category, hazardous materials
- **Transportation**: Truck type, capacity, special equipment, transport mode
- **Timeline**: Pickup/delivery dates, duration, distance
- **Budget**: Amount, currency, pricing model, payment terms
- **Provider**: Type, certifications, insurance, experience
- **Status**: Comprehensive workflow status management

#### rfq_mapping

- **parent_rfq_id**: Parent RFQ reference
- **child_rfq_id**: Child RFQ reference
- **relationship_type**: Type of relationship
- **sequence_order**: Ordering for segments

### Relationships

```
Demand RFQ (1) ←→ (Many) Segment RFQs
Segment RFQ (1) ←→ (Many) Requisition RFQs
Requisition RFQ (1) ←→ (Many) Individual RFQs
```

## Business Process Flow

### 1. Demand Creation

```
1. Create DEMAND type RFQ with ship from/to locations
2. Specify cargo details and truck requirements
3. Set timeline and budget
4. Submit for approval
```

### 2. Segment Planning

```
1. Create SEGMENT type RFQs linked to demand
2. Define segment types (first/middle/last mile)
3. Specify transport modes for each segment
4. Set segment-specific requirements
```

### 3. Requisition Creation

```
1. Create REQUISITION type RFQs linked to segments
2. Define transporter selection criteria
3. Set pricing models and provider requirements
4. Publish requisitions for responses
```

### 4. RFQ Distribution

```
1. Create individual RFQs from requisitions
2. Share with multiple transporters
3. Collect individual pricing
4. Evaluate and award contracts
```

## API Endpoints Structure

### RFQ Management

```
POST   /api/v1/rfqs                    # Create RFQ
GET    /api/v1/rfqs                    # List RFQs
GET    /api/v1/rfqs/{id}               # Get RFQ details
PUT    /api/v1/rfqs/{id}               # Update RFQ
DELETE /api/v1/rfqs/{id}               # Delete RFQ
POST   /api/v1/rfqs/{id}/submit        # Submit for approval
POST   /api/v1/rfqs/{id}/publish       # Publish RFQ
POST   /api/v1/rfqs/{id}/award         # Award RFQ
```

### RFQ Relationships

```
POST   /api/v1/rfqs/{id}/children      # Create child RFQ
GET    /api/v1/rfqs/{id}/children      # List child RFQs
GET    /api/v1/rfqs/{id}/parents       # List parent RFQs
POST   /api/v1/rfq-mappings            # Create mapping
GET    /api/v1/rfq-mappings            # List mappings
```

### Filtered Queries

```
GET    /api/v1/rfqs?type=DEMAND        # Get demand RFQs
GET    /api/v1/rfqs?type=SEGMENT       # Get segment RFQs
GET    /api/v1/rfqs?type=REQUISITION   # Get requisition RFQs
GET    /api/v1/rfqs?status=PUBLISHED   # Get published RFQs
```

## Configuration

### Rate Limiting

The system uses Resilience4j for rate limiting:

- **CREATE**: 10 requests per minute (RFQ creation)
- **READ**: 100 requests per minute (data retrieval)
- **ADMIN**: 20 requests per minute (updates, awards, deletions)

### Status Management

Each RFQ type has its own status workflow:

- **DEMAND**: DRAFT → SUBMITTED → APPROVED → IN_PROGRESS → AWARDED → COMPLETED
- **SEGMENT**: DRAFT → PUBLISHED → IN_PROGRESS → AWARDED → IN_TRANSIT → COMPLETED
- **REQUISITION**: DRAFT → PUBLISHED → IN_PROGRESS → AWARDED → COMPLETED

## Usage Examples

### Create a Demand RFQ

```bash
curl -X POST "http://localhost:8080/api/v1/rfqs" \
  -H "Content-Type: application/json" \
  -H "X-User-ID: user123" \
  -d '{
    "rfqType": "DEMAND",
    "title": "Electronics Shipment from China to USA",
    "description": "Shipment of electronic components",
    "shipFromAddress": "123 Factory Road",
    "shipFromCity": "Shenzhen",
    "shipFromCountry": "China",
    "shipToAddress": "456 Warehouse Ave",
    "shipToCity": "Los Angeles",
    "shipToCountry": "USA",
    "totalWeight": 5000.0,
    "weightUnit": "KG",
    "cargoType": "GENERAL_CARGO",
    "cargoCategory": "Electronics",
    "truckType": "CONTAINER_CHASSIS",
    "pickupDate": "2024-01-15T10:00:00",
    "deliveryDate": "2024-01-30T18:00:00",
    "budgetAmount": 15000.00,
    "priority": "HIGH"
  }'
```

### Create Segment RFQs for a Demand

```bash
curl -X POST "http://localhost:8080/api/v1/rfqs/1/children" \
  -H "Content-Type: application/json" \
  -H "X-User-ID: user123" \
  -d '{
    "rfqType": "SEGMENT",
    "title": "First Mile - Factory to Port",
    "sequenceOrder": 1,
    "shipFromAddress": "123 Factory Road",
    "shipFromCity": "Shenzhen",
    "shipFromCountry": "China",
    "shipToAddress": "Port of Shenzhen",
    "shipToCity": "Shenzhen",
    "shipToCountry": "China",
    "transportMode": "ROAD",
    "truckType": "CONTAINER_CHASSIS",
    "estimatedDurationHours": 4,
    "estimatedDistanceKm": 50.0,
    "relationshipType": "DEMAND_TO_SEGMENT"
  }'
```

### Create Requisition RFQ for a Segment

```bash
curl -X POST "http://localhost:8080/api/v1/rfqs/2/children" \
  -H "Content-Type: application/json" \
  -H "X-User-ID: user123" \
  -d '{
    "rfqType": "REQUISITION",
    "title": "Transportation Service for First Mile",
    "transportMode": "ROAD",
    "truckType": "CONTAINER_CHASSIS",
    "vehicleCount": 2,
    "totalWeight": 5000.0,
    "cargoType": "GENERAL_CARGO",
    "pickupDate": "2024-01-15T10:00:00",
    "deliveryDate": "2024-01-15T14:00:00",
    "pricingModel": "FIXED_PRICE",
    "providerType": "TRANSPORTER",
    "budgetAmount": 2000.00,
    "relationshipType": "SEGMENT_TO_REQUISITION"
  }'
```

### Create Individual RFQ for Pricing

```bash
curl -X POST "http://localhost:8080/api/v1/rfqs/3/children" \
  -H "Content-Type: application/json" \
  -H "X-User-ID: user123" \
  -d '{
    "rfqType": "REQUISITION",
    "title": "RFQ for First Mile Transportation",
    "description": "Request for quotation for first mile transportation",
    "priority": "HIGH",
    "validUntil": "2024-01-10T23:59:59",
    "relationshipType": "REQUISITION_TO_RFQ"
  }'
```

## Key Features

### 1. Consolidated Data Model

- Single table handles all RFQ types
- Comprehensive fields for all logistics requirements
- Flexible relationship mapping
- Efficient querying and indexing

### 2. Flexible Relationships

- RFQ mapping table for complex relationships
- Support for hierarchical structures
- Sequence ordering for segments
- Multiple relationship types

### 3. Comprehensive Cargo Management

- Detailed cargo specifications
- Hazardous material handling
- Temperature-controlled shipments
- Special equipment requirements

### 4. Multi-Modal Transportation

- Support for road, rail, air, sea, and multimodal transport
- Segment-specific transport mode selection
- Flexible routing and planning

### 5. Provider Management

- Multiple provider types
- Certification and insurance requirements
- Experience and qualification tracking

### 6. Pricing Flexibility

- Multiple pricing models
- Negotiable pricing options
- Budget tracking and management

### 7. Status Workflow

- Comprehensive status management
- Workflow progression tracking
- Audit trail for all status changes

## Security and Performance

### Security Features

- User authentication and authorization
- Rate limiting to prevent abuse
- Data validation and sanitization
- Audit trail for all operations

### Performance Optimizations

- Database indexing for common queries
- Pagination for large result sets
- Efficient relationship mapping
- Optimized status queries

## Future Enhancements

1. **Real-time Tracking**: Live shipment tracking integration
2. **Document Management**: Attach documents to RFQs
3. **Notification System**: Automated notifications for status changes
4. **Analytics Dashboard**: Performance and cost analytics
5. **Mobile App**: Mobile interface for transporters
6. **Integration APIs**: Third-party logistics provider integrations
7. **Machine Learning**: Predictive pricing and route optimization
8. **Blockchain**: Smart contracts for automated payments

This consolidated logistics RFQ system provides a streamlined solution for managing complex logistics operations with a single, comprehensive data model and flexible relationship management.
