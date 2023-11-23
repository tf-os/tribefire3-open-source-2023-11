package providers

import "github.com/pkg/errors"

// description of a database
type DatabaseDescriptor struct {
	ProjectId        string
	InstanceId       string
	DatabaseName     string
	DatabaseUser     string
	DatabasePassword string
	Database         interface{}
}

// Errors
var DatabaseAlreadyExists = errors.New("Database already exists")
var DatabaseDoesNotExist = errors.New("Database does not exist")
var UserAlreadyExist = errors.New("User already exists")
var UserDoesNotExist = errors.New("User does not exist")
var UnexpectedError = errors.New("Unexpected error")

type DatabaseProvider interface {
	CreateDatabase(desc *DatabaseDescriptor) (*DatabaseDescriptor, error)
	DeleteDatabase(desc *DatabaseDescriptor) error
	RetrieveDatabase(desc *DatabaseDescriptor) (*DatabaseDescriptor, error)
	CreateUser(desc *DatabaseDescriptor) (*DatabaseDescriptor, error)
	DeleteUser(desc *DatabaseDescriptor) error
	RetrieveUser(desc *DatabaseDescriptor) (*DatabaseDescriptor, error)
}
