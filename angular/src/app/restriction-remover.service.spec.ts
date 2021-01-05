import { TestBed } from '@angular/core/testing';

import { RestrictionRemoverService } from './restriction-remover.service';

describe('RestrictionRemoverService', () => {
  let service: RestrictionRemoverService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RestrictionRemoverService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
