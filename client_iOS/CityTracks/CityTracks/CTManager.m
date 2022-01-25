//
//  CTManager.m
//  CityTracks
//
//  Created by Carlos Quintella on 11/29/11.
//  Copyright (c) 2011 __MyCompanyName__. All rights reserved.
//

#import "CTManager.h"


/* sharedMyManager é uma variável utilizada para reter o singleton - É setada com nil a primeira vez que o método é chamado. */
static CTManager *myCTManager = nil;




@interface CTManager()
{

}







/* Métodos privados a classe CTManager */
- (BOOL) loadLocalConfiguration;



@end



@implementation CTManager
{

}

@synthesize reportingFrequency = _reportingFrequency;
@synthesize locationDBCacheSize = _locationDBCacheSize;
@synthesize accuracyLevel = _accuracyLevel;

@synthesize     serverName = _serverName;
@synthesize     deviceId = _deviceId;
@synthesize     locationDBPath = _locationDBPath;
@synthesize     myLocationDB = _myLocationDB;


-(BOOL) loadLocalConfiguration
{
    return 0;
}




+ (CTManager *)sharedInstance 
{

    @synchronized(self) {
        if (myCTManager == nil)
            myCTManager = [[super allocWithZone:NULL] init];
    }

    return myCTManager;
}


- (id)init {
    if (self = [super init]) {
        NSLog(@"INICIALIZANDO O CTMANAGER");
       
        //No caso de init do CTManager, carregamos a configuração local.
        [self loadLocalConfiguration];
        
        /* Verifica se os valores recuperados estão ok, caso contrário popula com o valor default. */
        
        if (self.deviceId==nil) {    
            self.deviceId = [[UIDevice currentDevice] uniqueDeviceIdentifier];
            NSLog(@"\t\tDevice id:%@", self.deviceId);
            //salvar deviceId no arquivo de configuração
        }
        
        
        if ([self locationDBPath] == nil){ 
            NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
            NSString *documentsDirectory = [paths objectAtIndex:0];
            self.locationDBPath=documentsDirectory;
      //      NSString *DBpath = [documentsDirectory stringByAppendingPathComponent:@"locationDbFile"];
        //    self.locationDBPath=DBpath;
        }
        
        if (self.locationDBCacheSize<=0)
            self.locationDBCacheSize=DEFAULT_LOCATION_CACHE_SIZE;
        
        if (self.accuracyLevel<=1)
            self.accuracyLevel = DEFAULT_ACCURACY_LEVEL;

        if(self.reportingFrequency<=0)
            self.reportingFrequency=DEFAULT_REPORTING_FREQUENCY;
        
        if(self.serverName==nil)
            self.serverName=@"172.30.182.152";
    }
    
    /* Cria o LocationDB e guarda em myLocationDB */
    self.myLocationDB = [[CTLocationDB alloc] initWithPath: self.locationDBPath andCacheSize:self.locationDBCacheSize];
    
    return self;
}














+ (id) hiddenalloc
{
    return [super alloc];
}

+(id) alloc
{
    return nil;   
}

+ (id)new {
    return [self alloc];
}

+ (id)allocWithZone:(NSZone *)zone {
    return [[self sharedInstance] alloc];
}

- (id)copyWithZone:(NSZone *)zone {
  
    NSLog(@"Error: Attempt to copy a singleton (%@).", [self description]);
    return self;
}


- (id)mutableCopyWithZone: (NSZone *)zone
{
    NSLog(@"Error: Attempt to copy a singleton (%@).", [self description]);
    return [self copyWithZone:zone];
    
}




@end





/*
 + (void)    userEmail:          (NSString *)newUserEmail
 {userEmail=newUserEmail;}
 
 + (NSString *) userEmail
 {return userEmail;}
 
 
 + (BOOL) StringIsValidEmail:(NSString *) checkString
 
 {  
 NSString *stricterFilterString = @"[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";  
 NSString *laxString = @".+@.+\\.[A-Za-z]{2}[A-Za-z]*";  
 NSString *emailRegex = stricterFilterString ? stricterFilterString : laxString;  
 NSPredicate *emailTest = [NSPredicate predicateWithFormat:@"SELF MATCHES %@", emailRegex];  
 return [emailTest evaluateWithObject:checkString];  
 }  
 
 */