//
//  CTConfigViewController.m
//  CityTracks
//
//  Created by Carlos Quintella on 11/29/11.
//  Copyright (c) 2011 __MyCompanyName__. All rights reserved.
//

#import "CTConfigViewController.h"
#import "CTViewController.h"





@implementation CTConfigViewController

@synthesize sliderReportingFrequency = _sliderReportingFrequency;
@synthesize sliderAccuracyLevel = _sliderAccuracyLevel;



- (IBAction)sliderPrecisionChanged:(UISlider *)sender {
    
    int newValue = round(sender.value);
    sender.value=newValue;
    [[CTManager sharedInstance] setAccuracyLevel:newValue];
   // [CTViewController changeTrackingAccurancy:[CTManager accuracyLevel]];
}


- (IBAction)sliderReportingFrequencyChanged:(UISlider *)sender {
    
    int newValue = round (sender.value);
    sender.value= newValue;
    [[CTManager sharedInstance] setReportingFrequency:newValue];    
  }





/*
- (IBAction)userEmailTextFieldChanged:(UITextField *)sender {
    
    NSString *newValue = (sender.text);
    if ([CTManager stringIsValidEmail:newValue])
    { 
        sender.text=newValue;
        [CTManager userEmail:newValue];
    }else{
        [CTManager userEmail:NULL];
        sender.text=@"invalid!";
    }
}
*/










- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    ;
    return self;
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

/*
// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView
{
}
*/

/*
// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad
{
    [super viewDidLoad];
}
*/

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}


- (void)viewWillAppear:(BOOL)animated{
    _sliderAccuracyLevel.value = [[CTManager sharedInstance] accuracyLevel];
    _sliderReportingFrequency.value =[[CTManager sharedInstance] reportingFrequency];
}


@end
