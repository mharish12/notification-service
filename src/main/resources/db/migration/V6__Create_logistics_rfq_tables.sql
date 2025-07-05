-- Create consolidated logistics_rfq table
CREATE TABLE logistics_rfq (
    id BIGSERIAL PRIMARY KEY,
    rfq_number VARCHAR(50) NOT NULL UNIQUE,
    rfq_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    sequence_order INTEGER,
    
    -- Location Details (for all types)
    ship_from_address VARCHAR(500),
    ship_from_city VARCHAR(100),
    ship_from_state VARCHAR(100),
    ship_from_country VARCHAR(100),
    ship_from_postal_code VARCHAR(20),
    ship_from_latitude DECIMAL(10, 8),
    ship_from_longitude DECIMAL(11, 8),
    
    ship_to_address VARCHAR(500),
    ship_to_city VARCHAR(100),
    ship_to_state VARCHAR(100),
    ship_to_country VARCHAR(100),
    ship_to_postal_code VARCHAR(20),
    ship_to_latitude DECIMAL(10, 8),
    ship_to_longitude DECIMAL(11, 8),
    
    -- Cargo Details
    total_weight DECIMAL(15, 3),
    weight_unit VARCHAR(10) DEFAULT 'KG',
    total_volume DECIMAL(15, 3),
    volume_unit VARCHAR(10) DEFAULT 'CBM',
    cargo_type VARCHAR(50),
    cargo_category VARCHAR(100),
    hazardous_material BOOLEAN NOT NULL DEFAULT FALSE,
    temperature_controlled BOOLEAN NOT NULL DEFAULT FALSE,
    min_temperature DECIMAL(5, 2),
    max_temperature DECIMAL(5, 2),
    pieces INTEGER,
    
    -- Transportation Requirements
    truck_type VARCHAR(50),
    truck_capacity DECIMAL(15, 3),
    special_equipment TEXT,
    transport_mode VARCHAR(50),
    vehicle_count INTEGER DEFAULT 1,
    
    -- Timeline
    pickup_date TIMESTAMP,
    delivery_date TIMESTAMP,
    flexible_pickup_window BOOLEAN DEFAULT FALSE,
    flexible_delivery_window BOOLEAN DEFAULT FALSE,
    estimated_duration_hours INTEGER,
    estimated_distance_km DECIMAL(10, 2),
    
    -- Budget and Pricing
    budget_amount DECIMAL(15, 2),
    currency VARCHAR(3) DEFAULT 'USD',
    pricing_model VARCHAR(50),
    payment_terms TEXT,
    
    -- Provider Requirements
    provider_type VARCHAR(50),
    required_certifications TEXT,
    insurance_requirements TEXT,
    experience_years INTEGER,
    
    -- Status and Priority
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    requested_by VARCHAR(255) NOT NULL,
    requested_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_until TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    metadata TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255)
);

-- Create rfq_mapping table for relationships
CREATE TABLE rfq_mapping (
    id BIGSERIAL PRIMARY KEY,
    parent_rfq_id BIGINT NOT NULL,
    child_rfq_id BIGINT NOT NULL,
    relationship_type VARCHAR(50) NOT NULL,
    sequence_order INTEGER,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    metadata TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    
    CONSTRAINT fk_rfq_mapping_parent FOREIGN KEY (parent_rfq_id) REFERENCES logistics_rfq(id),
    CONSTRAINT fk_rfq_mapping_child FOREIGN KEY (child_rfq_id) REFERENCES logistics_rfq(id),
    CONSTRAINT uk_rfq_mapping_unique UNIQUE (parent_rfq_id, child_rfq_id, relationship_type)
);

-- Create indexes for better performance
CREATE INDEX idx_logistics_rfq_number ON logistics_rfq(rfq_number);
CREATE INDEX idx_logistics_rfq_type ON logistics_rfq(rfq_type);
CREATE INDEX idx_logistics_rfq_status ON logistics_rfq(status, is_active);
CREATE INDEX idx_logistics_rfq_requested_by ON logistics_rfq(requested_by, is_active);
CREATE INDEX idx_logistics_rfq_pickup_date ON logistics_rfq(pickup_date);
CREATE INDEX idx_logistics_rfq_cargo_type ON logistics_rfq(cargo_type);
CREATE INDEX idx_logistics_rfq_truck_type ON logistics_rfq(truck_type);
CREATE INDEX idx_logistics_rfq_transport_mode ON logistics_rfq(transport_mode);

CREATE INDEX idx_rfq_mapping_parent ON rfq_mapping(parent_rfq_id, relationship_type);
CREATE INDEX idx_rfq_mapping_child ON rfq_mapping(child_rfq_id, relationship_type);
CREATE INDEX idx_rfq_mapping_type ON rfq_mapping(relationship_type, is_active);
CREATE INDEX idx_rfq_mapping_sequence ON rfq_mapping(parent_rfq_id, sequence_order);

-- Add comments for documentation
COMMENT ON TABLE logistics_rfq IS 'Consolidated RFQ table for all logistics requirements (demand, segment, requisition)';
COMMENT ON TABLE rfq_mapping IS 'Mapping table for relationships between different RFQ types';

COMMENT ON COLUMN logistics_rfq.rfq_number IS 'Unique RFQ identifier';
COMMENT ON COLUMN logistics_rfq.rfq_type IS 'RFQ type: DEMAND, SEGMENT, REQUISITION';
COMMENT ON COLUMN logistics_rfq.cargo_type IS 'Type of cargo: GENERAL_CARGO, BULK_CARGO, CONTAINERIZED, etc.';
COMMENT ON COLUMN logistics_rfq.truck_type IS 'Required truck type: FLATBED, BOX_TRUCK, REFRIGERATED, etc.';
COMMENT ON COLUMN logistics_rfq.transport_mode IS 'Transport mode: ROAD, RAIL, AIR, SEA, MULTIMODAL';
COMMENT ON COLUMN logistics_rfq.status IS 'RFQ status: DRAFT, SUBMITTED, APPROVED, PUBLISHED, IN_PROGRESS, AWARDED, IN_TRANSIT, COMPLETED, CANCELLED, EXPIRED, DELAYED';
COMMENT ON COLUMN logistics_rfq.pricing_model IS 'Pricing model: FIXED_PRICE, PER_KM, PER_WEIGHT, PER_VOLUME, PER_HOUR, NEGOTIABLE';
COMMENT ON COLUMN logistics_rfq.provider_type IS 'Provider type: TRANSPORTER, FREIGHT_FORWARDER, LOGISTICS_PROVIDER, WAREHOUSE, CUSTOMS_BROKER, INSURANCE_PROVIDER';

COMMENT ON COLUMN rfq_mapping.relationship_type IS 'Relationship type: DEMAND_TO_SEGMENT, SEGMENT_TO_REQUISITION, REQUISITION_TO_RFQ';
COMMENT ON COLUMN rfq_mapping.parent_rfq_id IS 'Parent RFQ ID';
COMMENT ON COLUMN rfq_mapping.child_rfq_id IS 'Child RFQ ID'; 