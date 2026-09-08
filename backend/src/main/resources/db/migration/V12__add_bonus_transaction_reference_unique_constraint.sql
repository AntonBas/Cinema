ALTER TABLE bonus_transactions
    ADD CONSTRAINT uk_bonus_transaction_card_type_reference UNIQUE (bonus_card_id, type, reference_id);
