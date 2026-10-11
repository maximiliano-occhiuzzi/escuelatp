import { Directive, Input, TemplateRef, ViewContainerRef } from '@angular/core';
import { AuthService } from '../service/auth.service';
import { Rol } from '../models/auth.model';

// Directiva estructural: muestra el elemento solo si el usuario tiene alguno
// de los roles indicados.
//   <button *hasRole="['PRECEPTOR']">Deshabilitar</button>
@Directive({
  selector: '[hasRole]'
})
export class HasRoleDirective {

  private visible = false;

  constructor(
    private templateRef: TemplateRef<unknown>,
    private viewContainer: ViewContainerRef,
    private authService: AuthService
  ) { }

  @Input() set hasRole(roles: Rol[]) {
    const permitido = this.authService.hasRole(...roles);
    if (permitido && !this.visible) {
      this.viewContainer.createEmbeddedView(this.templateRef);
      this.visible = true;
    } else if (!permitido && this.visible) {
      this.viewContainer.clear();
      this.visible = false;
    }
  }
}
