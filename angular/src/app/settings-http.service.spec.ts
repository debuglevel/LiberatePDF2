import { TestBed } from '@angular/core/testing';

import { SettingsHttpServiceService } from './settings-http.service';

describe('SettingsHttpServiceService', () => {
  let service: SettingsHttpServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SettingsHttpServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
