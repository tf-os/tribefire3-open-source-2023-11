package v1

import "github.com/pkg/errors"

var (
	WorkspaceNameError           = errors.New("Workspace name not supported")
	InitiativeNameError          = errors.New("Initiative name not supported")
	StageNameError               = errors.New("Stage name not supported")
	UnknownComponentError        = errors.New("Unknown component found")
	StatusUpdateNotPeristedError = errors.New("Status update not persisted")
	MissingCartridgeImage        = errors.New("Custom cartridge needs an image, please provide one")
	UnsupportedDatabaseError     = errors.New("Database type not supported. Only 'cloudsql' and 'local' are supported")
)
