# Database Schema Migration: Public to Endurance Schema

This document describes the migration from the `public` schema to the `endurance` schema for the Endurance application.

## Overview

The application has been updated to use a dedicated `endurance` schema instead of the default `public` schema. This provides better organization and isolation of application-specific database objects.

## Migration Files

The migration is handled by the following Liquibase changelog files:

1. **000-create-endurance-schema.xml** - Creates the endurance schema
2. **001-initial-schema.xml** - Creates all tables in the endurance schema
3. **002-migrate-to-endurance-schema.xml** - Migrates existing data from public to endurance schema
4. **003-cleanup-public-schema.xml** - Optional cleanup of old public schema tables

## What Changed

### Application Configuration
- Updated `application.yml` to set `default-schema: endurance`
- All JPA entities now specify `schema = "endurance"` in their `@Table` annotations

### Database Schema
- All tables now reside in the `endurance` schema
- Foreign key references updated to use fully qualified table names
- Indexes and constraints updated to use the endurance schema

### JPA Entities Updated
- `QuizEntity`
- `QuestionEntity`
- `QuestionOptionEntity`
- `PlayerEntity`
- `QuizPlayerEntity`
- `AnswerSubmissionEntity`

## Migration Process

### Automatic Migration
When you restart the application, Liquibase will automatically:

1. Create the `endurance` schema if it doesn't exist
2. Create all tables in the `endurance` schema
3. Migrate any existing data from the `public` schema to the `endurance` schema
4. Update sequences to maintain ID continuity

### Manual Verification
After migration, you can verify the data was moved correctly:

```sql
-- Check that tables exist in endurance schema
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'endurance' 
ORDER BY table_name;

-- Verify data migration
SELECT COUNT(*) FROM endurance.quiz;
SELECT COUNT(*) FROM endurance.question;
SELECT COUNT(*) FROM endurance.player;
-- etc.
```

### Cleanup (Optional)
After confirming successful migration, you can optionally remove the old public schema tables by:

1. Uncommenting the DROP statements in `003-cleanup-public-schema.xml`
2. Restarting the application to run the cleanup migration

**Warning**: Only do this after confirming all data has been successfully migrated and the application is working correctly.

## Rollback

If you need to rollback the migration:

1. Update `application.yml` to use `default-schema: public`
2. Remove the schema specification from all JPA entity `@Table` annotations
3. Drop the `endurance` schema: `DROP SCHEMA endurance CASCADE;`

## Benefits of the Endurance Schema

1. **Better Organization**: Application-specific tables are isolated from system tables
2. **Security**: Easier to manage permissions and access control
3. **Maintenance**: Clear separation between application and system database objects
4. **Scalability**: Easier to manage multiple applications on the same database

## Troubleshooting

### Common Issues

1. **Schema Not Found**: Ensure the database user has CREATE SCHEMA permissions
2. **Foreign Key Errors**: Verify all referenced tables exist in the endurance schema
3. **Sequence Issues**: Check that sequences are properly updated after migration

### Logs
Check the application logs for any Liquibase migration errors. The migration process will log each step of the process.

## Support

If you encounter issues during migration, check:
1. Application logs for detailed error messages
2. Database permissions for the application user
3. Existing data integrity in the public schema
