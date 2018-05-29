#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RNMistLibrary : RCTEventEmitter <RCTBridgeModule>
@property BOOL hasLogined;
@end
  
