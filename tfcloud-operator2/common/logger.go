package common

import (
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
)

var log *zap.SugaredLogger

//var logger *zap.Logger

//var log *logr.Logger

// setup logger one time with tag information
func SetupLogger(initiative, stage string) {

	config := zap.NewProductionConfig()
	if DevelopmentLogging() {
		config = zap.NewDevelopmentConfig()
		config.EncoderConfig.EncodeLevel = zapcore.CapitalColorLevelEncoder
	}

	config.Encoding = "console"
	config.InitialFields = map[string]interface{}{
		"initiative": initiative, "stage": stage,
	}

	// check log level
	if LogLevel() != "" {
		parsedLevel, err := zap.ParseAtomicLevel(LogLevel())
		if err != nil {
			zap.S().Panicf("Unknown log-level: %s", LogLevel())
		}

		config.Level = parsedLevel
		zap.S().Infof("Setting log-level to %s", LogLevel())
	}

	// activate json logging
	if JsonLoggingEnabled() {
		config.Encoding = "json"
	}

	// set that the calling function/line number is added
	if ExtendedLoggingEnabled() {
		config.DisableCaller = false
		config.DisableStacktrace = false
		zap.S().Infof("Logging with extended logger - file/line number will be displayed")
	}

	logger, _ := config.Build()
	if log == nil {
		sugar := logger.Named("tf-operator").Sugar()
		log = sugar
	}
}

func L() *zap.SugaredLogger {
	//defer logger.Sync()
	// for unit tests, we might not have initialized the logger
	if log == nil {
		_log, _ := zap.NewDevelopment()
		return _log.Sugar()
	}

	return log
}
